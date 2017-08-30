package com.yanxit.currencies.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;

public class Test {
    public static void main(String[] args){
        String s = "{\"disclaimer\": \"Usage subject to terms: https://openexchangerates.org/terms\",\"license\": \"https://openexchangerates.org/license\",\"timestamp\": 1503993600,\"base\": \"USD\",\"rates\": {\"AED\": 3.672896,\"AFN\": 68.4205}}";
        try {
            JsonNode o= new ObjectMapper().readTree(s);
            System.out.print(o.get("rates").get("AFN").asDouble()); ;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
