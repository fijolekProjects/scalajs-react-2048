# scalajs-react-2048
This is [2048](http://gabrielecirulli.github.io/2048/) game clone made using [scalajs-react](https://github.com/japgolly/scalajs-react) in just 350 lines of Scala and 250 lines of CSS. [Scala.js](http://www.scala-js.org/) is ordinary Scala code, but it compiles to JavaScript which is great. Yay! 

Demo: http://fijolekprojects.github.io/scalajs-react-2048/

### How it looks

![Game sample](http://i.imgur.com/9hCKLeA.png)

### How it works
##### Backend
Game logic is implemented in `game2048.Board` class. It's based on idea that making a move in game is kind of the same despite direction, so 'the real logic' has to be implemented only once, for one direction and rest is pretty trivial. I decided to implement `shiftLeft` method, which 'shifts' row to the left. In that situation `shiftRight` implementation is as simple as `reverse.shiftLeft.reverse`. The same holds for moving up and down, but instead of rows one needs to operate on whole board (because there's no 'up' and 'down' in row dimension), so for example `moveUp` (which moves whole board up) translates to `transpose.moveLeft.transpose`, where transpose is the same as [transposing a matrix](https://en.wikipedia.org/wiki/Transpose).

##### Frontend
Handling user input, managing game state and rendering takes place in `webapp.Game2048Webapp` and all of it is controlled by nice Scala interface for [React](https://facebook.github.io/react/) [scalajs-react](https://github.com/japgolly/scalajs-react). This class may look complicated at first, but it doesn't really do that much. The most important thing is setting CSS classes in `createTile` method, because all animations and tiles positions are represented using CSS.

### Building
Run
```
sbt ~fastOptJS
```
Game should be at [http://localhost:12345/src/main/html/index.html](http://localhost:12345/src/main/html/index.html) with 'reload on change' feature on.

### Credits
The game was originally created by [gabrielecirulli](http://gabrielecirulli.github.io/2048/).
