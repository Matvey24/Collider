package com.matvey.perelman.gdxcollider.scheduler.value;

import com.matvey.perelman.gdxcollider.scheduler.DoubleCallbackProvider;
import com.matvey.perelman.gdxcollider.scheduler.pools.Disposable;
import com.matvey.perelman.gdxcollider.scheduler.pools.ObjectPool;
import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskNode;
import com.matvey.perelman.gdxcollider.scheduler.task_scheduler.TaskScheduler;

import java.lang.Math;
import java.util.ArrayList;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

import static java.lang.Float.NaN;
import static com.matvey.perelman.gdxcollider.scheduler.value.Math.*;

public class Value extends DoubleCallbackProvider implements Disposable {
    private final ValuePool pool;
    private final ArrayList<Float> poly;
    private double start_at;
    private Runnable dispose_dependencies;
    public boolean temporary;

    private Value(ValuePool pool) {
        this.pool = pool;
        poly = new ArrayList<>();
        temporary = true;
    }

    /**
     * Use this method to clear value and then use it
     */
    @Override
    public void dispose() {
        if (dispose_dependencies != null)
            dispose_dependencies.run();
        dispose_dependencies = null;
        poly.clear();
        temporary = true;
    }

    /**
     * Use this method to free it in object pool, pool calls dispose() in this method
     */
    public void free() {
        pool.free(this);
    }

    @Override
    public void unregister(DoubleConsumer t) {
        super.unregister(t);
        if (temporary && !hasConsumers())
            pool.free(this);
    }

    public Value set_constant(float c, double start_at) {
        if (dispose_dependencies != null)
            dispose_dependencies.run();
        dispose_dependencies = null;
        this.poly.clear();
        if (c != 0)
            this.poly.add(c);
        this.start_at = start_at;
        temporary = false;
        this.provide(start_at);
        return this;
    }

    public Value set(Value other) {
        if (dispose_dependencies != null)
            dispose_dependencies.run();
        dispose_dependencies = null;
        temporary = false;
        DoubleConsumer update = (tm) -> {
            this.start_at = tm;
            this.poly.clear();
            this.poly.addAll(other.poly);
            provide(tm);
        };
        other.register(update);
        dispose_dependencies = () -> other.unregister(update);
        update.accept(other.start_at);
        return this;
    }

    public double start_at() {
        return start_at;
    }

    public float value_at_time(double time) {
        return calculate_poly(delta(time));
    }

    //start time of integral is where it equals zero
    public Value integral(double time_start) {
        Value integral = pool.get();
        integral.start_at = start_at;
        DoubleConsumer update = (tm) -> {
            float dt = integral.delta(tm);
            float old_value = integral.calculate_poly(dt);
            integral.start_at = tm;
            integral.poly.clear();
            integral.poly.add(old_value);
            for (int i = 0; i < poly.size(); ++i)
                integral.poly.add(poly.get(i) / (i + 1));
            integral.provide(tm);
        };
        register(update);
        integral.dispose_dependencies = () -> unregister(update);
        update.accept(time_start);
        return integral;
    }

    public Value multiply(Value by) {
        Value prod = pool.get();
        double max_time = Math.max(start_at, by.start_at);
        DoubleConsumer update = (tm) -> {
            int pow = poly.size() + by.poly.size() - 1;
            Value ok = start_at == tm ? this : by;
            Value off = ok == this ? by : this;
            float offset = off.delta(tm);
            prod.poly.clear();
            //((x + off)^2 + 2(x + off) + 1) * (4x + 7)
            for (int r = 0; r < pow; ++r) {
                float sum = 0;
                int cycleK = Math.min(r, ok.poly.size()) + 1;
                for (int k = Math.max(0, r - off.poly.size() + 1); k < cycleK; ++k)
                    sum += ok.poly.get(k) * (off.poly.get(r - k)
                            + (r - k + 1 != off.poly.size() ? (off.poly.get(r - k + 1) * offset) : 0));
                prod.poly.add(sum);
            }
            prod.provide(tm);
        };
        register(update);
        by.register(update);
        prod.dispose_dependencies = () -> {
            unregister(update);
            by.unregister(update);
        };

        update.accept(max_time);
        return prod;
    }

    public Value add(Value oth) {
        return sum(this, oth);
    }

    public static Value sum(Value... values) {
        assert values.length > 0;
        Value sum = values[0].pool.get();
        double max_time = 0;
        for (Value val : values)
            if (max_time < val.start_at)
                max_time = val.start_at;
        DoubleConsumer update = (tm) -> {
            sum.poly.clear();
            sum.start_at = tm;
            int max_pow = 0;
            for (Value val : values)
                if (max_pow < val.poly.size())
                    max_pow = val.poly.size();
            for (int pow = 0; pow < max_pow; ++pow)
                sum.poly.add(0f);
            for (Value val : values) {
                float delta = val.delta(tm);
                if (delta == 0) {
                    for (int pow = 0; pow < val.poly.size(); ++pow)
                        sum.poly.set(pow, sum.poly.get(pow) + val.poly.get(pow));
                    continue;
                }
                for (int c = 0; c < val.poly.size(); ++c) {
                    for (int pow = 0; pow <= c; ++pow) {
                        sum.poly.set(pow, sum.poly.get(pow) +
                                CNK(c, pow) * val.poly.get(c) * qpow(delta, c - pow));
                    }
                }
            }
            sum.provide(tm);
        };
        update.accept(max_time);
        for (Value v : values)
            v.register(update);
        sum.dispose_dependencies = () -> {
            for (Value v : values)
                v.unregister(update);
        };
        return sum;
    }

    public TaskNode run_when_reaches(float value, DoubleConsumer cons) {
        TaskNode task = pool.scheduler.createTask();

        DoubleConsumer updater = tm -> {
            double new_time = time_when(value);
            if (new_time == -1) {
                task.unpin();
                return;
            }
            task.pin(new_time);
        };
        register(updater);
        task.set(tm -> {
            unregister(updater);
            cons.accept(tm);
        });
        task.on_cancel = () -> unregister(updater);

        double time = time_when(value);
        if (time != -1)
            task.pin(time);
        return task;
    }

    public double time_when(float value) {
        float time = float_time_when(value, 0, 0);
        if (Float.isNaN(time))
            return -1;
        return start_at + time;
    }

    //use this method only if polynom is strictly monotonic in (start, end)
    float binary_search(float value, int nd, float start, float end, boolean raises) {
        float mid = start;
        for (int i = 0; i < 50; ++i) {
            mid = (start + end) / 2;
            float mid_val = calculate_derivative(mid, nd);
            if (start == mid || end == mid || mid_val == value)
                return mid;
            if (mid_val > value ^ raises)
                start = mid;
            else
                end = mid;
        }
        return mid;
    }

    // TODO add solutions for x^3 and x^4 polynoms
    float float_time_when(float value, int nd, float start) {
        int polysize = poly.size() - nd;
        while (coef(polysize, nd) == 0)
            polysize--;
        if (polysize > 3) {
            float zero_deriv = float_time_when(0, nd + 1, start);
            float start_val = calculate_derivative(start, nd);
            if (Float.isNaN(zero_deriv)) {//No extremum in (start, +inf), so function is strictly monotonic there
                float delta = 1;
                float end_val = calculate_derivative(start + delta, nd);
                if (Math.signum(calculate_derivative(end_val, nd + 1)) != Math.signum(value - start_val))
                    return NaN;
                boolean raises = value - start_val > 0;
                while (Math.signum(value - start_val) != Math.signum(end_val - value)) {
                    delta += 1;
                    end_val = calculate_derivative(start + delta, nd);
                }
                return binary_search(value, nd, start, start + delta, raises);
            }

            float end_val = calculate_derivative(zero_deriv, nd);
            if (Math.signum(start_val - value) != Math.signum(end_val - value))
                return binary_search(value, nd, start, zero_deriv, start_val < value);
            return float_time_when(value, nd, zero_deriv + zero_deriv / (1 << 20)); //mantissa - 23 bits
        }
        if (polysize == 3) {
            float disc = coef(1, nd) * coef(1, nd) - 4 * coef(2, nd) * (coef(0, nd) - value);
            if (disc < 0)
                return NaN;
            float sign = coef(2, nd) > 0 ? 1 : -1;
            float root = (-coef(1, nd) + sign * (float) Math.sqrt(disc)) / (2 * coef(2, nd));
            if (root > start)
                return root;
            return NaN;
        }
        if (polysize == 2 && coef(1, nd) != 0) {
            float root = -(coef(0, nd) - value) / coef(1, nd);
            if (root > start)
                return root;
        }
        return NaN;
    }

    private float delta(double time) {
        return (float) (time - start_at);
    }

    private float calculate_poly(float at) {
        float sum = 0;
        float xpow = 1;
        for (int i = 0; i < poly.size(); ++i) {
            sum += xpow * poly.get(i);
            xpow *= at;
        }
        return sum;
    }

    //nth derivative in point
    private float calculate_derivative(float at, int n) {
        float sum = 0;
        float xpow = 1;
        for (int pow = n; pow < poly.size(); ++pow) {
            sum += xpow * poly.get(pow) * fact(pow, n);
            xpow *= at;
        }
        //x^2 + 2x + 1, at point 1 -- deriv(1) = 2x + 2 = 4
        return sum;
    }

    private float coef(int coef, int nderiv) {
        if (nderiv == 0)
            return poly.get(coef);
        return poly.get(coef + nderiv) * fact(coef + nderiv, nderiv);
    }

    public static class ValuePool extends ObjectPool.DisposingPool<Value> {
        public final TaskScheduler scheduler;

        ValuePool(Supplier<Value> supplier, TaskScheduler scheduler) {
            super(supplier);
            this.scheduler = scheduler;
        }
    }

    public static ValuePool make_pool(TaskScheduler scheduler) {
        class PoolContainer {
            ValuePool pool;
        }
        PoolContainer cont = new PoolContainer();
        Supplier<Value> supplier = () -> new Value(cont.pool);
        cont.pool = new ValuePool(supplier, scheduler);
        return cont.pool;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Value{");
        if (poly.isEmpty()) {
            sb.append("0");
        }
        for (int i = poly.size() - 1; i >= 0; --i) {
            sb.append(poly.get(i));
            if (i != 0) {
                sb.append("t");
                if (i != 1) {
                    sb.append("^");
                    sb.append(i);
                }
                if (poly.get(i - 1) < 0)
                    sb.append(" ");
                else
                    sb.append(" +");
            }
        }
        sb.append(", start_at=");
        sb.append(start_at);
        if (temporary)
            sb.append(", temp");
        sb.append("}");
        return sb.toString();
    }
}
