// src/components/ListingSkeletonGrid.jsx
import React from 'react';
import { Card, Skeleton } from 'antd';

export default function ListingSkeletonGrid({ count = 6 }) {
  return (
    <div style={{ display:'grid', gap:20, gridTemplateColumns:'repeat(auto-fill,minmax(250px,1fr))' }}>
      {Array.from({ length: count }).map((_, i) => (
        <Card key={i} hoverable>
          <div style={{ width:'100%', position:'relative', paddingTop:'62%', background:'#f2f4f7', borderRadius:4, overflow:'hidden' }}>
            <Skeleton.Image active style={{ position:'absolute', inset:0, width:'100%', height:'100%' }} />
          </div>
          <div style={{ marginTop:12 }}>
            <Skeleton active title={{ width: '80%' }} paragraph={{ rows: 2, width: ['60%','40%'] }} />
          </div>
        </Card>
      ))}
    </div>
  );
}
