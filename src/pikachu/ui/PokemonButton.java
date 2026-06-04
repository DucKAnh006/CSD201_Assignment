package pikachu.ui;

import javax.swing.*;

public class PokemonButton extends JButton {
    private int row;
    private int col;
    private int imageID;

    public PokemonButton(int row, int col, int imageID) {
        super();
        this.row = row;
        this.col = col;
        this.imageID = imageID;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getImageID() {
        return imageID;
    }
    
    public void setImageID(int imageID) {
        this.imageID = imageID;
    }
}