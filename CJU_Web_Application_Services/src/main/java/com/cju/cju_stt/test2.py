import whisper
import sys


if __name__ == "__main__":
    file_name = sys.argv[0]
    model = whisper.load_model("base")
    #result = model.transcribe(file_name, fp16=False)

    try {
        result = model.transcribe(file_name, fp16=False)
        print(result)
    } except {
        print("error")
    }

