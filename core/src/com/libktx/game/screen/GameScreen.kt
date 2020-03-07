package com.libktx.game.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.TimeUtils
import com.libktx.game.Game
import ktx.app.KtxScreen

class GameScreen(val game: Game) : KtxScreen {
    // load the images for the chicken & dog, 64x64 pixels each
    private val chickenImage = Texture(Gdx.files.internal("images/roastchicken.png"))
    private val dogImage = Texture(Gdx.files.internal("images/dog.png"))
    // load the drop sound effect and the rain background music
    private val chickenSound = Gdx.audio.newSound(Gdx.files.internal("sounds/drop.wav"))
    private val rainMusic = Gdx.audio.newMusic(Gdx.files.internal("music/rain.mp3")).apply { isLooping = true }
    // The camera ensures we can render using our target resolution of 800x480
    //    pixels no matter what the screen resolution is.
    private val camera = OrthographicCamera().apply { setToOrtho(false, 800f, 480f) }
    // create a Rectangle to logically represent the dog
    // center the dog horizontally
    // bottom left dog corner is 20px above
    private val dog = Rectangle(800f / 2f - 64f / 2f, 20f, 64f, 64f)
    // create the touchPos to store mouse click position
    private val touchPos = Vector3()
    // create the chicken array and spawn the first chicken
    private val chicken = Array<Rectangle>() // gdx, not Kotlin Array
    private var lastChickenTime = 0L
    private var chickenGathered = 0

    private fun spawnChicken() {
        chicken.add(Rectangle(MathUtils.random(0f, 800f - 64f), 480f, 64f, 64f))
        lastChickenTime = TimeUtils.nanoTime()
    }

    override fun render(delta: Float) {
        // generally good practice to update the camera's matrices once per frame
        camera.update()

        // tell the SpriteBatch to render in the coordinate system specified by the camera.
        game.batch.projectionMatrix = camera.combined

        // begin a new batch and draw the dog and all chickens
        game.batch.begin()
        game.font.draw(game.batch, "Chicken Collected: $chickenGathered", 0f, 480f)
        game.batch.draw(dogImage, dog.x, dog.y, dog.width, dog.height)
        for (chicken in chicken) {
            game.batch.draw(chickenImage, chicken.x, chicken.y, dog.width, dog.height)//in this case dog.width, dog.height set the size of the chicken to be the same as the dog
        }
        game.batch.end()

        // process user input
        if (Gdx.input.isTouched) {
            touchPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
            camera.unproject(touchPos)
            dog.x = touchPos.x - 64f / 2f
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dog.x -= 200 * delta
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dog.x += 200 * delta
        }

        // make sure the dog stays within the screen bounds
        dog.x = MathUtils.clamp(dog.x, 0f, 800f - 64f)

        // check if we need to create a new chicken
        if (TimeUtils.nanoTime() - lastChickenTime > 1_000_000_000L) {
            spawnChicken()
        }

        // move the chickens, remove any that are beneath the bottom edge of the
        //    screen or that hit the dog.  In the latter case, play back a sound
        //    effect also
        val iter = chicken.iterator()
        while (iter.hasNext()) {
            val chicken = iter.next()
            chicken.y -= 200 * delta
            if (chicken.y + 64 < 0)
                iter.remove()

            if (chicken.overlaps(dog)) {
                chickenGathered++
                chickenSound.play()
                iter.remove()
            }
        }
    }

    override fun show() {
        // start the playback of the background music when the screen is shown
        rainMusic.play()
        spawnChicken()
    }

    override fun dispose() {
        chickenImage.dispose()
        dogImage.dispose()
        chickenSound.dispose()
        rainMusic.dispose()
    }
}
