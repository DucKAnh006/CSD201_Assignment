/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pikachu.ui;

import javax.swing.*;

/**
 *
 * @author ADMIN
 */
public class MenuUI extends JPanel {

    MainFrame parent;

    public MenuUI(MainFrame parent) {
        this.parent = parent;
        
        initUI();
    }

    private void initUI() {
        // thiet ke ui
        
        /*
            tao cac nut va event cua nut bao gom
            bat dau => (
                chon do kho
                de  : InGame game = new InGame(8, 12, parent);
                bth : InGame game = new InGame(10, 15, parent);
                kho : InGame game = new InGame(12, 18, parent);
            )
            thanh tuu
            thoat
            
        */
        JButton b = new JButton();
        this.add(b);
        b.addActionListener(l -> {
            InGame game = new InGame(2, 2, parent);
            parent.switchPanel(game);
        });
    }
}
