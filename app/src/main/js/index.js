/**
 * Created by sreejeshpillai on 09/05/15.
 */
var app = require('express')();
var cors = require('cors')
app.use(cors());
var http = require('http').Server(app);
var io = require('socket.io')(http);
app.get('/',function(req,res){
    res.sendFile(__dirname+'/index.html');
    next();
});
io.on('connection',function(socket){
    console.log('one user connected '+socket.id);
    /*socket.removeAllListeners('disconnect');
    process.nextTick(socket.disconnect.bind(socket));*/
    socket.on('users', function(){
        var sockets = io.sockets.sockets;
        if(sockets != null){
            sockets.forEach(function(sock){
                sock.emit('users',data);
            })
        }
    })
    socket.on('message',function(data){
        var sockets = io.sockets.sockets;
        if(sockets != null){
            sockets.forEach(function(sock){
                if(sock.id != socket.id)
                {
                    sock.emit('message',data);
                }
            })
        }
    },
    socket.on('loggedUser', function(data){
        console.log(JSON.stringify(data));
    }))
    socket.on('disconnect',function(){
        console.log('one user disconnected '+socket.id);
    })
})

http.listen(3000,function(){
    console.log('server listening on port 3000');
});