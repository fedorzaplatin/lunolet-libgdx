package com.fedorzaplatin.lunolet.stages;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Hud extends Stage{
    private Label velocity;
    private Label altitude;

    public Hud(Viewport viewport) {
        super(viewport);

        Table table = new Table();
        table.left().top();
        table.setFillParent(true);

        velocity = new Label("0", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        altitude = new Label("0", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        table.add(velocity).align(Align.left);
        table.row();
        table.add(altitude).align(Align.left);

        addActor(table);
    }

    public void update(Vector2 velocity, Vector2 altitude){
        this.velocity.setText(String.format("Velocity: %02.2f", velocity.len()));
        this.altitude.setText(String.format("Alt.: %03.2f", altitude.len()));
    }
}
