import whisper

model = whisper.load_model("base")
result = model.transcribe("광화문자생한방병원_1.mp3", fp16=False)

try {
    result = model.transcribe("광화문자생한방병원_1.mp3", fp16=False)
    print(result)
} except {
    print("error")
}

