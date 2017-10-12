package edu.neu.madcourse.hangxu;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageButton;
import android.graphics.PorterDuff.Mode;

/**
 * Class used for defining tile.
 */

public class Tile {

    private int charLevel;
    private int colorMode;
    private int large;
    private int small;
    private View view;
    private Tile[] subTiles;
    private final GameFragment gameFragment;

    public Tile(GameFragment gameFragment) {
        this.gameFragment = gameFragment;
    }

    public void setLarge(int large) {
        this.large = large;
    }

    public int getLarge() {
        return large;
    }

    public void setSmall(int small) {
        this.small = small;
    }

    public int getSmall() {
        return small;
    }

    public void setColorMode(int colorMode) {
        this.colorMode = colorMode;
    }

    public int getColorMode() {
        return colorMode;
    }

    public void setCharLevel(int charLevel) {
        this.charLevel = charLevel;
    }

    public int getCharLevel() {
        return charLevel;
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }

    public void setSubTiles(Tile[] subTiles) {
        this.subTiles = subTiles;
    }

    public Tile[] getSubTiles() {
        return subTiles;
    }

    public void updateDrawableState() {
        if (view == null) return;
        int level = getCharLevel();
        if (view.getBackground() != null) {
            view.getBackground().setLevel(level);
        }

        if (view instanceof ImageButton) {
            Drawable drawable = ((ImageButton) view).getDrawable();
            Mode mode = Mode.OVERLAY;
            view.setBackgroundColor(colorMode);
            drawable.setLevel(level);
        }
    }

    public void animate() {
        Animator animator = AnimatorInflater.loadAnimator(gameFragment.getActivity(), R.animator.scroggle);
        if (getView() != null) {
            animator.setTarget(getView());
            animator.start();
        }
    }
}
