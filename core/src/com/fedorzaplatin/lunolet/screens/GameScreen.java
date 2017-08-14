package com.fedorzaplatin.lunolet.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.fedorzaplatin.lunolet.MainClass;
import com.fedorzaplatin.lunolet.Moon;
import com.fedorzaplatin.lunolet.LunarModule;
import com.fedorzaplatin.lunolet.stages.Hud;

import static com.fedorzaplatin.lunolet.Constants.PPM;

public class GameScreen extends BaseScreen{

    private boolean DEBUG = true;

    static private int HEIGHT = Gdx.graphics.getHeight();
    static private int WIDTH = Gdx.graphics.getWidth();

    private World world;
    private Stage stage;
    private Hud hudStage;
    private LunarModule lunarModule;
    private Moon moon;

    private boolean isLanded;
    private float contactTime;

    private Box2DDebugRenderer b2ddr;

    public GameScreen(final MainClass game) {
        super(game);

        if (DEBUG) {
            b2ddr = new Box2DDebugRenderer(true,
                    true,
                    true,
                    true,
                    true,
                    true);
        }

        stage = new Stage(new FitViewport(WIDTH / PPM, HEIGHT / PPM), new PolygonSpriteBatch());
        stage.getCamera().position.set(new Vector2(WIDTH / 2 / PPM, HEIGHT / 2 / PPM), 0);

        world = new World(new Vector2(0, -1.62f), false);
        world.setContactListener(new GameContactListener());

        hudStage = new Hud(new FitViewport(WIDTH, HEIGHT), (BitmapFont) game.am.get("bebas.fnt"));
    }

    @Override
    public void show() {
        isLanded = false;
        contactTime = 0;

        Texture backgroundTexture = game.am.get("game-screen/background.png");
        Image backgroundImage = new Image(backgroundTexture);
        backgroundImage.setSize(backgroundTexture.getWidth() / PPM, backgroundTexture.getHeight() / PPM);
        backgroundImage.setPosition(0, 0);
        stage.addActor(backgroundImage);
        
        Texture moonSurface = game.am.get("game-screen/moonTexture.png");
        moon = new Moon(world, moonSurface, new Vector2(0, 0));
        stage.addActor(moon);

        Texture lunarModuleTexture = game.am.get("game-screen/lunarModuleTexture.png");
        TextureAtlas fireSprite = game.am.get("game-screen/fire.atlas");
        lunarModule = new LunarModule(world,
                lunarModuleTexture,
                fireSprite,
                new Vector2(WIDTH / PPM / 2, 1130 / PPM ),
                (Sound) game.am.get("game-screen/engineSound.mp3"));
        stage.addActor(lunarModule);

        Gdx.input.setInputProcessor(new GameScreenInputProcessor());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //check if lunar module has crossed the game world bounds
        if (lunarModule.getPosition().x < (-20 / PPM) |
                lunarModule.getPosition().x > (820 / PPM) |
                lunarModule.getPosition().y > (1250 / PPM)) {
            lunarModule.destroy();
        }

        if (!lunarModule.isAlive()) {
            game.setScreen(game.sm.gameOverScreen);
        }

        if (isLanded & lunarModule.getVelocity().len() < 0.01f){
            contactTime += delta;
        } else {
            contactTime = 0;
        }

        if (isLanded & contactTime > 1) {
            game.setScreen(game.sm.gameCompletedScreen);
        }

        if (lunarModule.getPosition().y * PPM > 900){
            stage.getCamera().position.set(new float[]{WIDTH / PPM / 2, 900 / PPM, 0});
        } else if ((400 < lunarModule.getPosition().y * PPM) & (lunarModule.getPosition().y * PPM < 900)){
            stage.getCamera().position.set(new float[]{WIDTH / PPM / 2, lunarModule.getPosition().y, 0});
        } else {
            stage.getCamera().position.set(new float[]{WIDTH / PPM / 2, 400 / PPM, 0});
        }

        hudStage.update(lunarModule.getVelocity(), lunarModule.getPosition(), lunarModule.getFuelMass());
        stage.act();
        world.step(1 / 60f, 6, 2);
        stage.draw();
        hudStage.draw();

        if (DEBUG) {
            b2ddr.render(world, stage.getCamera().combined);
        }
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        stage.clear();
        lunarModule.detach();
        moon.detach();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        world.dispose();
        super.dispose();
    }

    private class GameContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            if (lunarModule.getVelocity().len() > 3f | Math.abs(lunarModule.getAngle() - 90f) >= 13){
                lunarModule.destroy();
            } else {
                isLanded = true;
            }
        }


        @Override
        public void endContact(Contact contact) {
            isLanded = false;
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {

        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {

        }
    }

    private class GameScreenInputProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int i) {
            switch (i) {
                case Input.Keys.SPACE:
                    lunarModule.activateMainEngine();
                    break;
                case Input.Keys.A:
                    lunarModule.rotateLeft();
                    break;
                case Input.Keys.D:
                    lunarModule.rotateRight();
                    break;
            }
            return false;
        }

        @Override
        public boolean keyUp(int i) {
            switch(i){
                case Input.Keys.SPACE:
                    lunarModule.deactivateMainEngine();
                    break;
            }
            return false;
        }

        @Override
        public boolean keyTyped(char c) {
            return false;
        }

        @Override
        public boolean touchDown(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchUp(int i, int i1, int i2, int i3) {
            return false;
        }

        @Override
        public boolean touchDragged(int i, int i1, int i2) {
            return false;
        }

        @Override
        public boolean mouseMoved(int i, int i1) {
            return false;
        }

        @Override
        public boolean scrolled(int i) {
            return false;
        }
    }
}
