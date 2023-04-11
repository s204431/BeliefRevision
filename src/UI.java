import formulas.Formula;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.server.ExportException;

public class UI extends JPanel {

    private List list;
    private BeliefBase beliefBase;

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
        label.setBounds(100, 50, 500, 40);
        add(label);

        JTextField textField = new JTextField();
        textField.setBounds(100, 100, 500, 40);
        add(textField);

        JButton revisionButton = new JButton("Revision");
        revisionButton.setBounds(100, 150, 160, 40);
        revisionButton.addActionListener(e -> {
            try {
                beliefBase.revision(Formula.parseString(textField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        JButton expansionButton = new JButton("Expansion");
        expansionButton.addActionListener(e -> {
            try {
                beliefBase.expansion(Formula.parseString(textField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        expansionButton.setBounds(270, 150, 160, 40);
        JButton contractionButton = new JButton("Contraction");
        contractionButton.addActionListener(e -> {
            try {
                beliefBase.contraction(Formula.parseString(textField.getText()));
                updateList();
            } catch (Exception e1) {}
        });
        contractionButton.setBounds(440, 150, 160, 40);
        add(revisionButton);
        add(expansionButton);
        add(contractionButton);

        JLabel label2 = new JLabel("Current belief base:");
        label2.setFont(label2.getFont().deriveFont(20f));
        label2.setBounds(100, 230, 500, 40);
        add(label2);

        list = new List();
        list.setBounds(100, 280, 500, 300);
        add(list);
        updateList();

        repaint();
    }

    private void updateList() {
        list.removeAll();
        java.util.List<Formula> formulas = beliefBase.beliefBase;
        for (Formula formula : formulas) {
            list.add(formula.prettyPrint());
        }
        repaint();
    }
}
