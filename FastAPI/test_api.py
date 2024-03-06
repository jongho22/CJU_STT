from fastapi import FastAPI
from pydantic import BaseModel
import whisper
import torch
import os
from io import *
import tempfile
import base64

def STT_module(audio_with_header,model) :
    # 임시 파일 생성
    temp_file = tempfile.NamedTemporaryFile(suffix='.wav', delete=False)
    temp_file.write(audio_with_header)
    temp_path = temp_file.name

    # STT 실행
    result = model.transcribe(temp_path, fp16=False)

    # 임시 파일 삭제
    os.remove(temp_path)

    return result['text']

# os.environ['CUDA_LAUNCH_BLOCKING'] = "|1"
# os.environ["CUDA_VISIBLE_DEVICES"] = "0"

# FastAPI
app = FastAPI()

# 하드웨어 설정
DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 메인 모델 설정
MODEL = whisper.load_model("medium").to(DEVICE)

# 요청 구조
class RequestArgument(BaseModel):
    audioData: str

class RequestBody(BaseModel):
    argument: RequestArgument

@app.post("/uploadToFastAPI")
async def upload_to_fastapi(request_body: RequestBody):
    # 데이터 수신
    encode_audio = request_body.argument.audioData
    decoded_audio = base64.b64decode(encode_audio)
    return STT_module(decoded_audio,MODEL) 

    