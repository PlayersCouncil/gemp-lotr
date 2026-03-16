package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sooner", "103_118");
					put("merry", "1_302");
					put("sam", "1_311");
					put("pippin", "1_306");
					put("aragorn", "1_89");
					put("sword", "1_299");
					put("merryspipe", "103_113");
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheSoonerWereRidofItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: The Sooner We're Rid of It
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Hobbit companions.
		 * 	Skirmish: Exert a companion and hinder each card borne by them to make that companion strength +1 for each card hindered (+3 if that card is a weapon).  Hinder this condition if you can spot more than 4 companions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sooner");

		assertEquals("The Sooner We're Rid of It", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void TheSoonerWereRidofItHindersAttachmentsAndGrantsStrengthBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var merryspipe = scn.GetFreepsCard("merryspipe");
		var runner = scn.GetShadowCard("runner");

		// Merry with Hobbit Sword (weapon, +2 str) and Merry's Pipe (non-weapon)
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, sword, merryspipe);
		scn.MoveCardsToSupportArea(sooner);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass(); // move to site 3

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, runner);

		// Before ability: Merry base 3 + sword 2 = 5
		assertEquals(5, scn.GetStrength(merry));

		// Use ability: exert Merry, hinder sword + pipe
		scn.FreepsUseCardAction(sooner);
		scn.FreepsChooseCard(merry);

		// After: sword hindered (loses +2 base bonus), gains +3 (weapon) +1 (pipe) = net 7
		// Merry: 3 (base) + 0 (sword hindered) + 3 (weapon bonus) + 1 (pipe bonus) = 7
		assertTrue(scn.IsHindered(sword));
		assertTrue(scn.IsHindered(merryspipe));
		assertEquals(7, scn.GetStrength(merry));
		assertEquals(1, scn.GetWoundsOn(merry)); // exerted as cost
	}

	@Test
	public void TheSoonerWereRidofItHindersSelfWithFewCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");

		// 3 companions: Frodo + Merry + Sam, under 5
		scn.MoveCompanionsToTable(merry, sam);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(sooner);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, runner);

		scn.FreepsUseCardAction(sooner);
		scn.FreepsChooseCard(merry);

		// With <5 companions, condition is hindered (not discarded)
		assertTrue(scn.IsHindered(sooner));
		assertInZone(Zone.SUPPORT, sooner);
	}

	@Test
	public void TheSoonerWereRidofItDiscardsSelfWith5PlusCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sooner = scn.GetFreepsCard("sooner");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");

		// 5 companions: Frodo + Merry + Sam + Pippin + Aragorn
		scn.MoveCompanionsToTable(merry, sam, pippin, aragorn);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(sooner);

		scn.StartGame();

		scn.SkipToSite(2);
		scn.MoveMinionsToTable(runner);
		scn.FreepsPass();

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignAndResolve(merry, runner);

		scn.FreepsUseCardAction(sooner);
		scn.FreepsChooseCard(merry);

		// With 5+ companions, condition is discarded
		assertInDiscard(sooner);
	}
}
