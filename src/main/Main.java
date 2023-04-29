package main;

import formulas.*;
import masterMind.MasterMindGame;
import masterMind.MasterMindUI;

public class Main {

    public static void main(String[] args) {
        BeliefBase beliefBase = new BeliefBase();
        new UI(beliefBase);
    }
}