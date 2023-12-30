package com.example.kotlinsocketio

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.kotlinsocketio.ui.theme.KotlinSocketIOTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinSocketIOTheme {
                val uri = URI.create("http://10.0.2.2:3001")
                val options = IO.Options.builder().build()
                val socket = IO.socket(uri, options)
                val chat = remember {
                    mutableStateListOf<String>()
                }
                DisposableEffect("test") {
                    socket.connect()
                    socket.on(Socket.EVENT_CONNECT) {
                        Log.w("myApp", "connected")
                        chat.add("connected")
                    }
                    socket.on("chat message", Emitter.Listener {
                        chat.add(it[0] as String)
                    })
                    onDispose {
                        socket.disconnect()
                        socket.off("connect")
                    }
                }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var chatInput by remember {
                        mutableStateOf("")
                    }
                    val onChatInputChange = { text: String ->
                        chatInput = text
                    }
                    val emitEvent = {
                        socket.emit("chat message", chatInput)
                        chatInput = ""
                    }

                    val httpClient = HttpClient(CIO)
                    fun testHttp() {
                        var res = ""
                        runBlocking {
                            launch {
                                val testUri = URL("http://10.0.2.2:3001/test")
                                res = httpClient.get(testUri).body<String>()
                            }
                        }
                        Log.w("myApp", res)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        TextField(value = chatInput, onValueChange = onChatInputChange)
                        Text(text = chatInput)
                        Row {
                            Button(onClick = emitEvent) {
                                Text(text = "Submit")
                            }
                            Button(onClick = {
                                testHttp()
                            }) {
                                Text(text = "Test HTTP")
                            }
                        }
                        LazyColumn() {
                            items(chat) {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KotlinSocketIOTheme {
        Greeting("Android")
    }
}