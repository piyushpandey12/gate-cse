import React, { useMemo } from "react";
import katex from "katex";

interface FormulaMathProps {
  math: string;
  block?: boolean;
  className?: string;
}

export const FormulaMath: React.FC<FormulaMathProps> = ({
  math,
  block = false,
  className = "",
}) => {
  const html = useMemo(() => {
    try {
      return katex.renderToString(math, {
        displayMode: block,
        throwOnError: false,
      });
    } catch {
      return math;
    }
  }, [math, block]);

  return (
    <span
      className={`inline-block select-text ${className}`}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
};
