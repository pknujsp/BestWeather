package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateView extends View {
	private final FragmentType fragmentType;

	private final TextPaint dateTextPaint;
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int textHeight;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E");
	private List<DateValue> dateValueList;
	private int currentX;
	private int firstColX;

	public DateView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		dateTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		dateTextPaint.setTextAlign(Paint.Align.CENTER);
		dateTextPaint.setTextSize(context.getResources().getDimension(R.dimen.dateValueTextSizeInSCD));
		dateTextPaint.setColor(AppTheme.getTextColor(context, fragmentType));

		Rect rect = new Rect();
		LocalDateTime now = LocalDateTime.now();
		String val = now.format(dateTimeFormatter);
		dateTextPaint.getTextBounds(val, 0, val.length(), rect);
		textHeight = rect.height();

		setWillNotDraw(false);
	}

	public void init(List<LocalDateTime> dateTimeList) {
		LocalDateTime date = LocalDateTime.of(dateTimeList.get(0).toLocalDate(), dateTimeList.get(0).toLocalTime());
		LocalDateTime lastDate = LocalDateTime.of(date.toLocalDate(), date.toLocalTime());

		List<DateView.DateValue> dateValueList = new ArrayList<>();
		int beginX = 0;

		for (int col = 0; col < dateTimeList.size(); col++) {
			date = LocalDateTime.of(dateTimeList.get(col).toLocalDate(), dateTimeList.get(col).toLocalTime());

			if (date.getHour() == 0 || col == 0) {
				if (dateValueList.size() > 0) {
					dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (col - 1) + columnWidth / 2;
				}
				beginX = columnWidth * col + columnWidth / 2;
				dateValueList.add(new DateView.DateValue(beginX, date));
			}

			if (lastDate.getDayOfYear() != date.getDayOfYear()) {
				lastDate = date;
			}
		}
		dateValueList.get(dateValueList.size() - 1).endX = columnWidth * (dateTimeList.size() - 1) + columnWidth / 2;

		this.dateValueList = dateValueList;
		this.firstColX = dateValueList.get(0).beginX;
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int y = getHeight() / 2 + textHeight / 2;

		for (DateValue dateValue : dateValueList) {
			if (currentX >= dateValue.beginX - firstColX && currentX < dateValue.endX - firstColX) {
				dateValue.lastX = currentX + firstColX;
			} else if (currentX < dateValue.beginX) {
				dateValue.lastX = dateValue.beginX;
			}
			canvas.drawText(dateValue.date.format(dateTimeFormatter), dateValue.lastX, y, dateTextPaint);
		}
	}

	public void reDraw(int newX) {
		this.currentX = newX;
		invalidate();
	}


	public static class DateValue {
		public final int beginX;
		public final LocalDateTime date;
		public int endX;
		public int lastX;

		public DateValue(int beginX, LocalDateTime date) {
			this.beginX = beginX;
			this.date = date;
			this.lastX = beginX;
		}
	}
}