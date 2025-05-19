'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface FormData {
  id: number;
  name: string;
  idNumber: string;
  phoneNumber: string;
  address: string;
  workUnit: string;
  position: string;
  remark: string;
  imagePath: string;
  createdAt: string;
  updatedAt: string;
}

export default function DetailPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const [formData, setFormData] = useState<FormData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchFormData = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/forms/${params.id}`);
        if (!response.ok) {
          throw new Error('获取数据失败');
        }
        const data = await response.json();
        setFormData(data);
        setError(null);
      } catch (err) {
        console.error('获取表单数据出错:', err);
        setError('获取表单数据失败，请稍后再试');
      } finally {
        setLoading(false);
      }
    };

    fetchFormData();
  }, [params.id]);

  const handleDelete = async () => {
    if (!confirm('确定要删除这条记录吗？')) {
      return;
    }

    try {
      const response = await fetch(`/api/forms/${params.id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        throw new Error('删除失败');
      }

      alert('删除成功');
      router.push('/');
    } catch (err) {
      console.error('删除出错:', err);
      alert('删除失败，请稍后再试');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen p-6 bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-indigo-500 border-t-transparent"></div>
          <p className="mt-2 text-gray-600">加载中...</p>
        </div>
      </div>
    );
  }

  if (error || !formData) {
    return (
      <div className="min-h-screen p-6 bg-gray-50 flex flex-col items-center justify-center">
        <div className="text-center mb-6">
          <p className="text-red-500 text-lg">{error || '未找到表单数据'}</p>
        </div>
        <Link href="/" className="text-indigo-600 hover:text-indigo-900">
          返回首页
        </Link>
      </div>
    );
  }

  return (
    <main className="min-h-screen p-6 bg-gray-50">
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800">表单详情</h1>
          <Link href="/" className="text-indigo-600 hover:text-indigo-900">
            返回列表
          </Link>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-md mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-4">
              <div>
                <h3 className="text-sm font-medium text-gray-500">ID</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.id}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">姓名</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.name}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">身份证号</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.idNumber}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">联系电话</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.phoneNumber}</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <h3 className="text-sm font-medium text-gray-500">住址</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.address}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">工作单位</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.workUnit}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">职务</h3>
                <p className="mt-1 text-lg text-gray-900">{formData.position}</p>
              </div>
              <div>
                <h3 className="text-sm font-medium text-gray-500">创建时间</h3>
                <p className="mt-1 text-lg text-gray-900">{new Date(formData.createdAt).toLocaleString()}</p>
              </div>
            </div>
          </div>

          <div className="mt-6">
            <h3 className="text-sm font-medium text-gray-500">备注</h3>
            <p className="mt-1 text-lg text-gray-900">{formData.remark || '无'}</p>
          </div>
        </div>

        <div className="flex justify-end space-x-4">
          <Link
            href={`/edit/${params.id}`}
            className="px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
          >
            编辑
          </Link>
          <button
            onClick={handleDelete}
            className="px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
          >
            删除
          </button>
        </div>
      </div>
    </main>
  );
}