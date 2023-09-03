package com.voxeldev.usbkeyboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.voxeldev.usbkeyboard.ui.theme.USBKeyboardTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            USBKeyboardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Type text to send to the connected USB device")

                        TextField { enteredValue ->
                            if (enteredValue.isNotEmpty()) keyboardToUsb(enteredValue)
                        }

                        FlowRow(
                            modifier = Modifier.padding(all = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            maxItemsInEachRow = 3
                        ) {
                            KeyButton(text = "Space", key = SPACE_KEY)
                            KeyButton(text = "Enter", key = ENTER_KEY)
                            KeyButton(text = "Tab", key = TAB_KEY)
                            KeyButton(text = "Escape", key = ESCAPE_KEY)
                            KeyButton(text = "Backspace", key = BACKSPACE_KEY)
                        }

                        FlowRow(
                            modifier = Modifier.padding(all = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            maxItemsInEachRow = 3
                        ) {
                            KeyButton(text = "LMB", key = LMB_KEY, isMouseKey = true)
                            KeyButton(text = "RMB", key = RMB_KEY, isMouseKey = true)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TextField(onChangeCallback: (String) -> Unit) {
        val text by rememberSaveable { mutableStateOf("") }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = 48.dp,
                    end = 48.dp
                ),
            value = text,
            onValueChange = onChangeCallback,
        )
    }

    @Composable
    private fun KeyButton(text: String, key: String, isMouseKey: Boolean = false) =
        Button(
            modifier = Modifier.padding(all = 8.dp),
            enabled = true,
            onClick = { if (isMouseKey) mouseToUsb(key) else keyboardToUsb(key) },
        ) {
            Text(text = text)
        }

    private fun keyboardToUsb(key: String) {
        val addLeftShift = key.length == 1 && key[0].isUpperCase()

        val echoContent = "${if (addLeftShift) "$LEFT_SHIFT_KEY " else ""} ${if (addLeftShift) key.toLowerCase(Locale.current) else key}"
        val command = "$ECHO_COMMAND $echoContent | $HID_GADGET_TEST_PATH $KEYBOARD_PATH"

        if (ExecuteAsRootUtil.canRunRootCommands())
            ExecuteAsRootUtil.execute(command)
    }

    private fun mouseToUsb(key: String) {
        val command = "$ECHO_COMMAND $key | $HID_GADGET_TEST_PATH $MOUSE_PATH"

        if (ExecuteAsRootUtil.canRunRootCommands())
            ExecuteAsRootUtil.execute(command)
    }

    companion object {
        private const val SPACE_KEY = "space"
        private const val ENTER_KEY = "enter"
        private const val TAB_KEY = "tab"
        private const val ESCAPE_KEY = "esc"
        private const val BACKSPACE_KEY = "bckspc"
        private const val LEFT_SHIFT_KEY = "left-shift"

        private const val LMB_KEY = "--b1"
        private const val RMB_KEY = "--b2"

        private const val ECHO_COMMAND = "echo"
        private const val HID_GADGET_TEST_PATH = "/data/local/tmp/hid-gadget-test"
        private const val KEYBOARD_PATH = "/dev/hidg0 keyboard"
        private const val MOUSE_PATH = "/dev/hidg1 mouse"
    }
}
