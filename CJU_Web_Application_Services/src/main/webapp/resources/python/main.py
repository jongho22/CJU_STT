import whisper
import sys
import torch
import os
from datetime import datetime
import time

if __name__ == "__main__":
    # device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    
    model = whisper.load_model("medium")
    # model = model.to(device)

    file_name = "광화문자생한방병원_1.mp3"
    file_path = "/home/jongho/바탕화면/테스트 음성파일/"
    

    # start = time.time()
    # result = model.transcribe(f"{file_path}{file_name}", fp16=False)
    # #print(os.getcwd())
    # print(result['segments'])
    # end = time.time()

    # print(start-end)

    audio = whisper.load_audio(f"{file_path}{file_name}")
    audio = whisper.pad_or_trim(audio)

    mel = whisper.log_mel_spectrogram(audio).to(model.device)

    _, probs = model.detect_language(mel)
    print(f"Detected language: {max(probs, key=probs.get)}")

    options = whisper.DecodingOptions()
    result = whisper.decode(model, mel, options)

    print(result.text)