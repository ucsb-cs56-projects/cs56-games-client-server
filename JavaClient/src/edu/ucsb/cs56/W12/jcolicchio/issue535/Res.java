package edu.ucsb.cs56.W12.jcolicchio.issue535;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.*;

/**
 * stores resources for recycling
 * currently, just has chess pieces image, but will soon have more images and sounds for audio notification
 *
 * @author Joseph Colicchio
 * @version for CS56, Choice Points, Winter 2012
 */

public class Res {
    public static Image ChessPieces;

    public Res() {

    }

    public static void init(Class frame) {
        ChessPieces = null;
        try {
            ChessPieces = ImageIO.read(frame.getResource("/graphics/pieces.png"));

        } catch (IOException e) {
            System.out.println("oops");
        }
    }
}