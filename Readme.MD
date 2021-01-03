
#### Mục đích của cái này

Đọc truyện tranh trên Kindle Paper White tại sao không?

#### Thư viện

Đọc rendered HTML - https://jsoup.org/

Tạo file PDF - https://github.com/jasoet/fun-pdf 


#### Hiện tại chỉ hỗ trợ

http://www.hamtruyentranh.net/

#### Cần cài wkhtmltopdf
```
brew install wkhtmltopdf --cask
```

#### Json mẫu để thêm truyện vào menu

```
{
    "story_path": "",
    "story_name": "",
    "chapter_list_pages": "",
    "chapter_list_pages_back_up": "",
    "story_list_chapters": ""
}
```

#### Các bước để thêm một truyện mới
- Sửa file ham_truyen_tranh_net.json bằng cách copy Json mẫu ở trên
- Vào trang truyện để lấy đúng Selector điền vào file Json
- Chạy ứng dụng, chọn truyện cần tạo file PDF
- Nhập `from` bắt đầu lấy từ chapter index nào
- Nhập `limit` số chapter cần lấy tính từ chapter index

#### Video hướng dẫn

https://photos.app.goo.gl/vyQbGmwpgUwxSsmR6
