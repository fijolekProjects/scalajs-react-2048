var myboard = [[0, 0, 0, 0], [0, 0, 2, 0], [0, 0, 4, 0], [0, 0, 0, 0]];
console.log('huhu');

var Row = React.createClass({
    render: function () {
        var rowTemplate = this.props.fields.map(function (field, index) {
            var key = index;
            if (field !== 0) {
                return <div className="field" key={key}>{field}</div>;
            } else {
                return <div className="field" key={key}></div>;
            }
        });
        return (
            <span>{rowTemplate}</span>
        );
    }
});

var Board = React.createClass({
    keys:  {
        65: 'left',
        37: 'left',
        68: 'right',
        39: 'right',
        83: 'down',
        40: 'down',
        87: 'up',
        38: 'up'
    },

    getInitialState: function () {
        return {rows: this.props.rows};
    },

    componentDidMount: function () {
        window.addEventListener('keydown', this.moveBoard);
    },

    moveBoard: function (event) {
        var direction = this.keys[event.which];
        if (direction == 'left') {
            this.changeBoard([[2, 0, 0, 0], [2, 0, 0, 0], [2, 0, 0, 0], [2, 0, 0, 0]]);
        } else if (direction == 'right') {
            this.changeBoard([[0, 0, 0, 2], [0, 0, 0, 2], [0, 0, 0, 2], [0, 0, 0, 2]]);
        } else if (direction == 'up') {
            this.changeBoard([[2, 2, 2, 2], [0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0]]);
        } else if (direction == 'down') {
            this.changeBoard([[0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0], [2, 2, 2, 2]]);
        } else {
            console.log('nie ruszam');
        }
    },

    render: function () {
        var board = this.state.rows.map(function (row, index) {
            return <Row key={index} fields={row}/>
        });
        return (
            <div className="board">{board}</div>
        );
    },

    changeBoard: function (newRows) {
        this.setState({rows: newRows});
    }
});

var Counter = React.createClass({
    keys:  {
        65: 'left',
        37: 'left',
        68: 'right',
        39: 'right',
        83: 'down',
        40: 'down',
        87: 'up',
        38: 'up'
    },

    getInitialState: function() {
        return { count: 0 }
    },

    componentDidMount: function () {
        window.addEventListener('keydown', this.move);
    },

    move: function(event) {
        var direction = this.keys[event.which];
        if (direction == 'left') {
            this.incrCounter()
        }
    },

    incrCounter: function() {
      this.setState({ count: this.state.count + 1});
    },
    render: function() {
        return (
            <span>
                <span>{this.state.count}</span>
                <button type="button" onClick={this.incrCounter}>zwiekszaj</button>
            </span>
        );
    }
});

ReactDOM.render(
    <Counter />,
    document.getElementById('counter')
);

ReactDOM.render(
    <Board rows={myboard}/>,
    document.getElementById('board')
);


var HelloMessage = React.createClass({displayName: 'HelloMessage',
    render: function() {
        return React.createElement("div", null, "Hello ", this.props.name);
    }
});

ReactDOM.render(React.createElement(HelloMessage, {name: "John"}), document.getElementById("hellomessage"));