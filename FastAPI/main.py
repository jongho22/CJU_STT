from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import whisper
import sys
import torch

app = FastAPI()

DEVICE = torch.device("cuda")
model = whisper.load_model("medium")
MODEL = model.to(DEVICE)

class RequestArgument(BaseModel):
    filePath: str

class RequestBody(BaseModel):
    argument: RequestArgument

@app.post("/uploadToFastAPI")
async def upload_to_fastapi(request_body: RequestBody):
    # HTTP POST 요청 본문에서 전송된 데이터 추출
    file_path = request_body.argument.filePath
    
    resultText = MODEL.transcribe(f"{file_path}", fp16=False)
    print(resultText)
    return resultText