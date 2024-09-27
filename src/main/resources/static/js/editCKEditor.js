import {
	DecoupledEditor,
	AccessibilityHelp,
	Alignment,
	AutoImage,
	AutoLink,
	Autosave,
	BlockQuote,
	Bold,
	CloudServices,
	CodeBlock,
	Essentials,
	FontBackgroundColor,
	FontColor,
	FontFamily,
	FontSize,
	Heading,
	Highlight,
	HorizontalLine,
	ImageBlock,
	ImageCaption,
	ImageInline,
	ImageInsertViaUrl,
	ImageResize,
	ImageStyle,
	ImageTextAlternative,
	ImageToolbar,
	ImageUpload,
	Indent,
	IndentBlock,
	Italic,
	Link,
	LinkImage,
	List,
	ListProperties,
	MediaEmbed,
	Paragraph,
	SelectAll,
	Subscript,
	Superscript,
	Table,
	TableProperties,
	TableToolbar,
	TextTransformation,
	TodoList,
	Underline,
	Undo
} from 'ckeditor5';

import translations from 'ckeditor5/translations/ko.js';

const editorConfig = {
	toolbar: {
		items: [
			'undo',
			'redo',
			'|',
			'heading',
			'|',
			'fontSize',
			'fontFamily',
			'fontColor',
			'fontBackgroundColor',
			'|',
			'bold',
			'italic',
			'underline',
			'|',
			'link',
			'insertTable',
			'highlight',
			'blockQuote',
			'codeBlock',
			'|',
			'alignment',
			'|',
			'bulletedList',
			'numberedList',
			'todoList',
			'outdent',
			'indent'
		],
		shouldNotGroupWhenFull: false
	},
	plugins: [
		AccessibilityHelp,
		Alignment,
		AutoImage,
		AutoLink,
		Autosave,
		BlockQuote,
		Bold,
		CloudServices,
		CodeBlock,
		Essentials,
		FontBackgroundColor,
		FontColor,
		FontFamily,
		FontSize,
		Heading,
		Highlight,
		HorizontalLine,
		ImageBlock,
		ImageCaption,
		ImageInline,
		ImageInsertViaUrl,
		ImageResize,
		ImageStyle,
		ImageTextAlternative,
		ImageToolbar,
		ImageUpload,
		Indent,
		IndentBlock,
		Italic,
		Link,
		LinkImage,
		List,
		ListProperties,
		MediaEmbed,
		Paragraph,
		SelectAll,
		Subscript,
		Superscript,
		Table,
		TableProperties,
		TableToolbar,
		TextTransformation,
		TodoList,
		Underline,
		Undo
	],
	fontFamily: {
		supportAllValues: true
	},
	fontSize: {
		options: [10, 12, 14, 'default', 18, 20, 22],
		supportAllValues: true
	},
	heading: {
		options: [
			{
				model: 'paragraph',
				title: 'Paragraph',
				class: 'ck-heading_paragraph'
			},
			{
				model: 'heading1',
				view: 'h1',
				title: 'Heading 1',
				class: 'ck-heading_heading1'
			},
			{
				model: 'heading2',
				view: 'h2',
				title: 'Heading 2',
				class: 'ck-heading_heading2'
			},
			{
				model: 'heading3',
				view: 'h3',
				title: 'Heading 3',
				class: 'ck-heading_heading3'
			},
			{
				model: 'heading4',
				view: 'h4',
				title: 'Heading 4',
				class: 'ck-heading_heading4'
			},
			{
				model: 'heading5',
				view: 'h5',
				title: 'Heading 5',
				class: 'ck-heading_heading5'
			},
			{
				model: 'heading6',
				view: 'h6',
				title: 'Heading 6',
				class: 'ck-heading_heading6'
			}
		]
	},
	image: {
		toolbar: [
			'toggleImageCaption',
			'imageTextAlternative',
			'|',
			'imageStyle:inline',
			'imageStyle:wrapText',
			'imageStyle:breakText',
			'|',
			'resizeImage'
		]
	},
	initialData: '',
	language: 'ko',
	link: {
		addTargetToExternalLinks: true,
		defaultProtocol: 'https://',
		decorators: {
			toggleDownloadable: {
				mode: 'manual',
				label: 'Downloadable',
				attributes: {
					download: 'file'
				}
			}
		}
	},
	list: {
		properties: {
			styles: true,
			startIndex: true,
			reversed: true
		}
	},
	menuBar: {
		isVisible: true
	},
	placeholder: '여기에 작성하세요.',
	table: {
		contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties']
	},
	translations: [translations],
	extraPlugins: [ MyCustomUploadAdapterPlugin ],
	ckfinder: {
	            uploadUrl: 'C:/upload/portfolio/'
	        }
};

DecoupledEditor.create(document.querySelector('#editor'), editorConfig)
    .then(editor => {
        // 툴바와 메뉴바를 원하는 DOM 요소에 추가
        document.querySelector('#editor-toolbar').appendChild(editor.ui.view.toolbar.element);
        document.querySelector('#editor-menu-bar').appendChild(editor.ui.view.menuBarView.element);
        
        // 폼 제출 시 에디터 데이터를 textarea로 복사
        document.querySelector('form').addEventListener('submit', (event) => {
            document.querySelector('#portfolioDescription').value = editor.getData();
        });
		
		// 기존 내용을 에디터에 설정
		var content = document.getElementById('portfolioDescription').value;
		        editor.setData(content);
				
        window.editor = editor;  // 전역으로 에디터 참조 저장
    })
    .catch(error => {
        console.error('There was a problem initializing the editor.', error);
    });
	        
	// 커스텀 업로드 어댑터
	function MyCustomUploadAdapterPlugin(editor) {
	    editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
	        return new MyUploadAdapter(loader);
	    };
	}

	class MyUploadAdapter {
	    constructor(loader) {
	        this.loader = loader;
	    }

	    upload() {
	        return this.loader.file
	            .then(file => {
	                const data = new FormData();
	                data.append('upload', file);

	                return fetch('/portfolio/upload-image', {  // 서버 업로드 엔드포인트 설정
	                    method: 'POST',
	                    body: data
	                })
	                .then(response => response.json())
	                .then(result => {
	                    return {
	                        default: result.url  // 서버에서 반환된 이미지 URL
	                    };
	                });
	            });
	    }

	    abort() {
	        // 업로드 중단 처리
	    }
	}
