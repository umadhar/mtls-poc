package com.example.demo.java8;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Java8MethodCheatSheet {
    public static void main(String args[]){
        System.out.println("START ------ START");

        List<Employee> emplist = new EmployeeDataBase().getAllEmployees();

        //System.out.println(emplist);

        //Normal way
        System.out.println("NORMAL WAY  ------ START");
        for(Employee emp: emplist){
            //System.out.println(emp.getName());
        }

        //Foreach way
        System.out.println("FOR EACH WAY ------ START");
        //emplist.forEach(emp -> System.out.println(emp.getName()));

        //Stream way
        System.out.println("STREAM WAAY END ------ END");
        //emplist.stream().map(Employee::getName).forEach(System.out::println);

        //Filter with Development team
        //collect
        System.out.println("Development team filter WAAY  ------ END");
//        emplist.stream()
//                .filter(e-> e.getDept().equals("Development") && e.getSalary() > 85000)
//                .forEach(System.out::println);

        List<Employee> deptCollection = emplist.stream()
                                .filter(e-> e.getDept().equals("Development") && e.getSalary() > 85000)
                                .collect(Collectors.toList());

        Set<Employee> deptCollectionSet = emplist.stream()
                                    .filter(e-> e.getDept().equals("Development") && e.getSalary() > 85000)
                                    .collect(Collectors.toSet());

        Map<Integer, String> deptCollectionMap = emplist.stream()
                .filter(e-> e.getDept().equals("Development") && e.getSalary() > 85000)
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        System.out.println(deptCollectionMap);

        //map & distinct
//        emplist.stream()
//                .map(Employee::getDept)
//                .distinct()
//                .forEach(System.out::println);

        //nested list
        List<Stream<String>> lls =  emplist.stream()
                                            .map( e -> e.getProjects()
                                            .stream().map(p -> p.getName())).collect(Collectors.toList());

        //lls.stream().forEach(l-> l.forEach(System.out::println));

        System.out.println("flatMap  ------ END");

        Set<String> ll3 = emplist.stream().flatMap(e -> e.getProjects()
                .stream()
                .map(p->p.getName())).collect(Collectors.toSet());

        //ll3.stream().forEach(System.out::println);

        Map<String, List<Employee>> listByGroup = emplist.stream()
                .collect(Collectors.groupingBy(Employee::getGender));

        //System.out.println(listByGroup);

        Map<String, List<String>>  groupbyGenderNames = emplist.stream()
                .collect(Collectors.groupingBy(Employee::getGender, Collectors.mapping(Employee::getName, Collectors.toList())));

        //System.out.println(groupbyGenderNames);

        Employee findAny = emplist.stream()
                .findAny()
                .orElseThrow(()-> new IllegalArgumentException("No any employee found"));


        //System.out.println(findAny);

        boolean anymatch = emplist.stream()
                .anyMatch(e->e.getDept().equals("Development"));

        System.out.println(anymatch);

        boolean allmatch = emplist.stream()
                .allMatch(e->e.getDept().equals("Development"));
        System.out.println(allmatch);

        boolean nonematch = emplist.stream()
                .noneMatch(e-> e.getDept().equals("Developments"));

        System.out.println(nonematch);

        List<Employee> limit = emplist.stream()
                .skip(2)
                .collect(Collectors.toList());

        System.out.println(limit);


        List<Integer> listOfIntegers = Arrays.asList(71, 18, 42, 21, 67, 32, 95, 14, 56, 87);

        Map<Boolean, List<Integer>> groupby = listOfIntegers.stream()
                .collect(Collectors.groupingBy(e -> e % 2 == 0));

        System.out.println(groupby);

        Map<Boolean, List<Integer>> partitionby = listOfIntegers.stream()
                .collect(Collectors.partitioningBy(e -> e % 2 == 0));

        for(Map.Entry<Boolean, List<Integer>> entry: partitionby.entrySet()){
            if(entry.getKey()){
                System.out.println("EvenNumbers");
                entry.getValue().forEach(System.out::println);

            }else{
                System.out.println("Odd Numbers");
                entry.getValue().forEach(System.out::println);
            }

        }

        String inputString = "Java Concept Of The Day";

      







    }
}

