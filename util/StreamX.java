package eta.util;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class StreamX {
    public StreamX() {}

    public @NonNull Stream<double[]> ListToStreamDoubleArray(List<List<Double>> list) {
        Stream<List<Double>> stream = list.stream();
        return stream.map(inList -> inList.stream()
                .mapToDouble(Double::doubleValue)
                .toArray());
    }

    public @NonNull List<Double> zipApply(List<Double> list1, List<Double> list2, BinaryOperator<Double> func) {
        if (list1.size() != list2.size()) {
            throw new IllegalArgumentException("Lists must have the same length");
        }
        return IntStream.range(0, list1.size())
                .mapToObj(i -> func.apply(list1.get(i), list2.get(i)))
                .collect(Collectors.toList());
    }

    public @NonNull List<Double> OperateOnNestedListDouble(List<List<Double>> lists, BinaryOperator<Double> func) {
        if (lists.size() != 2) {
            throw new IllegalArgumentException("Exactly two lists required");
        }
        return zipApply(lists.get(0), lists.get(1), func);
    }

    public @NonNull DoubleStream StringToDoubleStream(String in, Function<Double, Double> func) {
        String[] splitStr = in.split("\\s*,\\s*");
        ToDoubleFunction<String> fun = Double::parseDouble;
        double[] StrAsDouble = Arrays.stream(splitStr)
                .mapToDouble(fun)
                .toArray();
        DoubleStream stream = Arrays.stream(StrAsDouble);
        return stream.map(func::apply);
    }

    public double @NonNull [][] IntStreamToDoubleArray(IntStream stream, IntFunction<double[]> func) {
        return stream.mapToObj(func).toArray(double[][]::new);
    }

    public @NonNull DoubleStream IntStreamDoubleStream(IntStream stream, IntToDoubleFunction func) {
        return stream.mapToDouble(func);
    }

    public @NonNull DoubleStream ArrayToDoubleStream(double[] array, Function<Double, Double> func) {
        DoubleStream stream = Arrays.stream(array);
        return stream.map(func::apply);
    }

    public @NonNull Stream<Double> ArrayToStreamDouble(double[] array, Function<Double, Double> func) {
        DoubleStream stream = ArrayToDoubleStream(array, func);
        return stream.boxed();
    }

    public @NonNull Map<String, Double> MapFlattening(Map<String, Map<String, Double>> map) {
        Stream<Map.Entry<String, Map<String, Double>>> stream = map.entrySet().stream();
        Function<Map.Entry<String, Map<String, Double>>, Stream<Map.Entry<String, Double>>> func1 =
                entry -> entry.getValue().entrySet().stream();
        Stream<Map.Entry<String, Double>> inDoubles = stream.flatMap(func1);
        return inDoubles.collect(
                Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue
        ));
    }

    public @NonNull DistMap DistMapFlattening(Map<String, Map<String, Double>> map) {
        return new DistMap(MapFlattening(map));
    }

    public @NonNull List<Map<String, Double>> ListOfDistMapToListOfMap(List<DistMap> map) {
        Stream<DistMap> stream = map.stream();
        Function<DistMap, Map<String, Double>> func = mapDist ->
                mapDist.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue
                                )
                        );
        return stream.map(func).toList();
    }
}
