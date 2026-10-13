import React, { useState } from 'react';
import { formatCurrency } from '../utils/formatters';

export const FinancialChart = ({ data = [], title = "Monthly Credit & Repayment Trends" }) => {
  const [hoveredIndex, setHoveredIndex] = useState(null);

  if (!data || data.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6 flex items-center justify-center h-64 text-slate-400">
        No transaction history available to generate chart
      </div>
    );
  }

  // Find max value for scaling
  const maxVal = Math.max(
    ...data.map((d) => Math.max(Number(d.creditGiven || 0), Number(d.paymentReceived || 0))),
    1000
  );

  const chartHeight = 180;
  const chartWidth = 560;
  const barGroupWidth = chartWidth / data.length;
  const barWidth = Math.min(22, (barGroupWidth - 24) / 2);

  return (
    <div className="bg-white rounded-xl border border-slate-200/80 p-5 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-4 border-b border-slate-100 gap-2">
        <h4 className="font-semibold text-slate-800 text-sm sm:text-base">{title}</h4>
        <div className="flex items-center gap-4 text-xs">
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-sky-500 inline-block"></span>
            <span className="text-slate-600 font-medium">Credit Given</span>
          </div>
          <div className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-emerald-500 inline-block"></span>
            <span className="text-slate-600 font-medium">Payments Received</span>
          </div>
        </div>
      </div>

      <div className="pt-4 relative">
        <svg
          viewBox={`0 0 ${chartWidth} ${chartHeight + 40}`}
          className="w-full h-56 overflow-visible"
        >
          {/* Background Grid Lines */}
          {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
            const y = chartHeight - ratio * chartHeight;
            const gridVal = maxVal * ratio;
            return (
              <g key={i}>
                <line
                  x1="0"
                  y1={y}
                  x2={chartWidth}
                  y2={y}
                  stroke="#f1f5f9"
                  strokeDasharray="4 4"
                />
                <text
                  x="0"
                  y={y - 4}
                  fill="#94a3b8"
                  fontSize="9"
                  className="select-none"
                >
                  ₹{Math.round(gridVal / 1000)}k
                </text>
              </g>
            );
          })}

          {/* Render Bars for each month */}
          {data.map((item, index) => {
            const credit = Number(item.creditGiven || 0);
            const payment = Number(item.paymentReceived || 0);

            const creditHeight = (credit / maxVal) * chartHeight;
            const paymentHeight = (payment / maxVal) * chartHeight;

            const groupCenterX = index * barGroupWidth + barGroupWidth / 2;
            const creditX = groupCenterX - barWidth - 2;
            const paymentX = groupCenterX + 2;

            const isHovered = hoveredIndex === index;

            return (
              <g
                key={index}
                onMouseEnter={() => setHoveredIndex(index)}
                onMouseLeave={() => setHoveredIndex(null)}
                className="cursor-pointer transition-opacity"
              >
                {/* Hover column background */}
                {isHovered && (
                  <rect
                    x={index * barGroupWidth + 4}
                    y="0"
                    width={barGroupWidth - 8}
                    height={chartHeight}
                    fill="#f8fafc"
                    rx="6"
                  />
                )}

                {/* Credit Bar (Sky) */}
                <rect
                  x={creditX}
                  y={chartHeight - creditHeight}
                  width={barWidth}
                  height={Math.max(creditHeight, 2)}
                  rx="3"
                  fill="#0284c7"
                  className="transition-all duration-300"
                />

                {/* Payment Bar (Emerald) */}
                <rect
                  x={paymentX}
                  y={chartHeight - paymentHeight}
                  width={barWidth}
                  height={Math.max(paymentHeight, 2)}
                  rx="3"
                  fill="#10b981"
                  className="transition-all duration-300"
                />

                {/* Month Label */}
                <text
                  x={groupCenterX}
                  y={chartHeight + 22}
                  textAnchor="middle"
                  fill={isHovered ? "#0f172a" : "#64748b"}
                  fontSize="11"
                  fontWeight={isHovered ? "600" : "500"}
                >
                  {item.month}
                </text>
              </g>
            );
          })}
        </svg>

        {/* Dynamic Tooltip on Hover */}
        {hoveredIndex !== null && data[hoveredIndex] && (
          <div className="mt-2 p-2.5 bg-slate-900 text-white rounded-lg shadow-lg text-xs flex items-center justify-between">
            <span className="font-semibold text-slate-200">{data[hoveredIndex].month} Summary</span>
            <div className="flex gap-4">
              <span className="text-sky-300">
                Credit: <strong>{formatCurrency(data[hoveredIndex].creditGiven)}</strong>
              </span>
              <span className="text-emerald-300">
                Paid: <strong>{formatCurrency(data[hoveredIndex].paymentReceived)}</strong>
              </span>
              <span className="text-slate-300">
                Net: <strong>{formatCurrency(data[hoveredIndex].netDifference)}</strong>
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
