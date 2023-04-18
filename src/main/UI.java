package main;

import formulas.Formula;
import masterMind.MasterMindGame;
import masterMind.MasterMindUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JPanel {

    private List list;
    private BeliefBase beliefBase;
    private MasterMindGame masterMindGame;
    private MasterMindUI masterMindUI;

    public UI(BeliefBase beliefBase) {
        this.beliefBase = beliefBase;
        JFrame frame = new JFrame("Belief Revision");
        frame.setPreferredSize(new Dimension(700, 700));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(null);
        frame.add(this);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        JLabel label = new JLabel("Write formula (use !, &, |, ->, <->):");
        label.setFont(label.getFont().deriveFont(20f));
        label.setBounds(100, 50, 400, 40);
        add(label);

        JLabel label2 = new JLabel("Priority:");
        label2.setFont(label2.getFont().deriveFont(20f));
        label2.setBounds(520, 50, 80, 40);
        add(label2);

        JTextField textField = new JTextField();
        textField.setBounds(100, 100, 400, 40);
        add(textField);

        JTextField priorityTextField = new JTextField("0");
        priorityTextField.setBounds(520, 100, 80, 40);
        add(priorityTextField);

        JButton revisionButton = new JButton("Revision");
        revisionButton.setBounds(100, 150, 120, 40);
        revisionButton.addActionListener(e -> {
            try {
                beliefBase.revision(Formula.parseString(textField.getText()), Integer.parseInt(priorityTextField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        JButton expansionButton = new JButton("Expansion");
        expansionButton.addActionListener(e -> {
            try {
                beliefBase.expansion(Formula.parseString(textField.getText()), Integer.parseInt(priorityTextField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        expansionButton.setBounds(230, 150, 120, 40);
        JButton contractionButton = new JButton("Contraction");
        contractionButton.addActionListener(e -> {
            try {
                beliefBase.contraction(Formula.parseString(textField.getText()), Integer.parseInt(priorityTextField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        contractionButton.setBounds(360, 150, 120, 40);

        JLabel entailmentLabel = new JLabel("", SwingConstants.CENTER);
        entailmentLabel.setFont(new Font("Arial", Font.BOLD, 18));
        entailmentLabel.setBounds(490, 185, 140, 40);
        entailmentLabel.setAlignmentX(CENTER_ALIGNMENT);
        entailmentLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));

        JButton entailmentButton = new JButton("Check entailment");
        entailmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (textField.getText().isEmpty()) {
                        entailmentLabel.setText("");
                        return;
                    }
                    boolean entailsFormula = beliefBase.entailsFormula(Formula.parseString(textField.getText()));
                    entailmentLabel.setForeground(entailsFormula ? new Color(1, 150, 32) : Color.RED);
                    entailmentLabel.setText(entailsFormula ? "True" : "False");
                    repaint();
                } catch (Exception ex) {}
            }
        });
        entailmentButton.setBounds(490, 150, 140, 40);
        add(revisionButton);
        add(expansionButton);
        add(contractionButton);
        add(entailmentButton);
        add(entailmentLabel);


        JLabel label3 = new JLabel("Current belief base:");
        label3.setFont(label3.getFont().deriveFont(20f));
        label3.setBounds(100, 230, 500, 40);
        add(label3);

        JButton masterMindButton = new JButton("MasterMind");
        masterMindButton.addActionListener(e -> {
            if (masterMindUI != null) {
                masterMindUI.close();
            }
            beliefBase.reset();
            masterMindGame = new MasterMindGame();
            masterMindUI = new MasterMindUI(masterMindGame, beliefBase, this);
            updateList();
            repaint();
        });
        masterMindButton.setBounds(frame.getPreferredSize().width / 2 - 80,  600, 160, 40);
        add(masterMindButton);

        list = new List();
        list.setBounds(100, 280, 500, 300);
        add(list);
        updateList();

        repaint();
    }

    public void updateList() {
        list.removeAll();
        java.util.List<Formula> formulas = beliefBase.beliefBase;
        for (int i = 0; i < formulas.size(); i++) {
            list.add("["+beliefBase.priorities.get(i) + "] " + formulas.get(i).prettyPrint());
        }
        repaint();
    }
}
