type AlertVariant = "error" | "success";

const STYLES: Record<AlertVariant, string> = {
  error: "bg-red-50 border-red-200 text-red-600",
  success: "bg-green-50 border-green-200 text-green-700",
};

interface AlertProps {
  variant: AlertVariant;
  message: string;
  className?: string;
}

const Alert =({ variant, message, className = "mb-5" }: AlertProps) => (
    <div className={`border text-sm px-4 py-3 rounded-xl ${STYLES[variant]} ${className}`}>
      {message}
    </div>
  );

export default Alert;
