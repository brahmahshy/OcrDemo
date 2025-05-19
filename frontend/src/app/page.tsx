'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';

interface FormData {
  id: number;
  name: string;
  idNumber: string;
  phoneNumber: string;
  address: string;
  workUnit: string;
  position: string;
  remark: string;
  createdAt: string;
  updatedAt: string;
}

export default function Home() {
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [formDataList, setFormDataList] = useState<FormData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [ocrTypes, setOcrTypes] = useState<string[]>([]);
  const [selectedOcrType, setSelectedOcrType] = useState<string>('');

  // 加载表单数据列表和OCR类型
  useEffect(() => {
    fetchFormData();
    fetchOcrTypes();
  }, []);

  // 获取可用的OCR类型
  const fetchOcrTypes = async () => {
    try {
      const response = await fetch('/api/forms/ocr-types');
      if (!response.ok) {
        throw new Error('获取OCR类型失败');
      }
      const data = await response.json();
      setOcrTypes(data.ocrTypes);
      if (data.ocrTypes.length > 0) {
        setSelectedOcrType(data.ocrTypes[0]); // 默认选择第一个OCR类型
      }
    } catch (err) {
      console.error('获取OCR类型出错:', err);
    }
  };

  // 获取表单数据
  const fetchFormData = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/forms');
      if (!response.ok) {
        throw new Error('获取数据失败');
      }
      const data = await response.json();
      setFormDataList(data);
      setError(null);
    } catch (err) {
      console.error('获取表单数据出错:', err);
      setError('获取表单数据失败，请稍后再试');
    } finally {
      setLoading(false);
    }
  };

  // 处理文件选择
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFile(selectedFile);
      // 创建预览
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result as string);
      };
      reader.readAsDataURL(selectedFile);
    }
  };

  // 处理表单提交
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file) {
      alert('请选择一个文件');
      return;
    }

    try {
      setUploading(true);
      const formData = new FormData();
      formData.append('file', file);
      
      // 添加OCR类型参数
      if (selectedOcrType) {
        formData.append('ocrType', selectedOcrType);
      }

      const response = await fetch('/api/forms/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error('上传失败');
      }

      const result = await response.json();
      console.log('上传成功:', result);
      
      // 重置表单
      setFile(null);
      setPreview(null);
      if (e.target instanceof HTMLFormElement) {
        e.target.reset();
      }
      
      // 刷新数据列表
      fetchFormData();
    } catch (err) {
      console.error('上传出错:', err);
      alert('上传失败，请稍后再试');
    } finally {
      setUploading(false);
    }
  };

  // 处理OCR类型选择
  const handleOcrTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedOcrType(e.target.value);
  };

  // 导出Excel
  const handleExport = () => {
    window.location.href = '/api/forms/export';
  };

  return (
    <main className="min-h-screen p-6 bg-gray-50">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-3xl font-bold text-center mb-8 text-gray-800">表单扫描系统</h1>
        
        {/* 上传表单区域 */}
        <div className="bg-white p-6 rounded-lg shadow-md mb-8">
          <h2 className="text-xl font-semibold mb-4 text-gray-700">上传表单图片</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
              <input
                type="file"
                id="file-upload"
                accept="image/*"
                onChange={handleFileChange}
                className="hidden"
              />
              {preview ? (
                <div className="relative w-full h-64 mb-4">
                  <Image 
                    src={preview} 
                    alt="预览" 
                    fill 
                    style={{ objectFit: 'contain' }} 
                  />
                </div>
              ) : (
                <div className="py-8">
                  <svg
                    className="mx-auto h-12 w-12 text-gray-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                    />
                  </svg>
                  <p className="mt-2 text-sm text-gray-600">点击选择或拖放图片到此处</p>
                </div>
              )}
              <label
                htmlFor="file-upload"
                className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 cursor-pointer"
              >
                选择图片
              </label>
            </div>
            {/* OCR类型选择 */}
            <div className="mb-4">
              <label htmlFor="ocr-type" className="block text-sm font-medium text-gray-700 mb-2">OCR识别方式</label>
              <select
                id="ocr-type"
                value={selectedOcrType}
                onChange={handleOcrTypeChange}
                className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
                disabled={uploading}
              >
                {ocrTypes.length === 0 ? (
                  <option value="">加载中...</option>
                ) : (
                  ocrTypes.map((type) => (
                    <option key={type} value={type}>
                      {type === 'tesseract' ? 'Tesseract OCR' : 
                       type === 'enhanced_tesseract' ? '增强版Tesseract OCR' : 
                       type === 'ai_ocr' ? 'AI OCR识别' : type}
                    </option>
                  ))
                )}
              </select>
              <p className="mt-1 text-sm text-gray-500">选择不同的OCR识别方式可能会影响识别效果</p>
            </div>
            
            <div className="flex justify-end">
              <button
                type="submit"
                disabled={!file || uploading || !selectedOcrType}
                className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {uploading ? '上传中...' : '上传并识别'}
              </button>
            </div>
          </form>
        </div>

        {/* 表单数据列表 */}
        <div className="bg-white p-6 rounded-lg shadow-md">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-semibold text-gray-700">表单数据列表</h2>
            <button
              onClick={handleExport}
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              导出Excel
            </button>
          </div>
          
          {loading ? (
            <div className="text-center py-8">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-indigo-500 border-t-transparent"></div>
              <p className="mt-2 text-gray-600">加载中...</p>
            </div>
          ) : error ? (
            <div className="text-center py-8 text-red-500">{error}</div>
          ) : formDataList.length === 0 ? (
            <div className="text-center py-8 text-gray-500">暂无数据</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">姓名</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">身份证号</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">联系电话</th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {formDataList.map((formData) => (
                    <tr key={formData.id}>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formData.id}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{formData.name}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formData.idNumber}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{formData.phoneNumber}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <Link href={`/detail/${formData.id}`} className="text-indigo-600 hover:text-indigo-900 mr-4">
                          查看详情
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
