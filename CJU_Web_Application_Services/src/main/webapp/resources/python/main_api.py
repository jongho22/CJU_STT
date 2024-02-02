import whisper
import sys
import torch
import os

if __name__ == "__main__":
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    
    model = whisper.load_model("medium")
    model = model.to(device)

    file_name = sys.argv[1]
    file_path = sys.argv[2]
    
    try :
        result = model.transcribe(f"{file_path}{file_name}", fp16=False)
        #print(os.getcwd())
        print(result['text'])
    except :
        print("다른 작업으로 인해 서버가 과부하 되었습니다.")
        #print("check" + os.getcwd())
    
    
