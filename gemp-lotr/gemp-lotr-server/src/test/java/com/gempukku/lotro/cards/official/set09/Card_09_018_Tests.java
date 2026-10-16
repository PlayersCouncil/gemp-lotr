package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dagger", "9_18");
					put("merry", "1_302");        // Merry, Friend to Sam: [shire] Hobbit, strength 3, vitality 4; his skirmish ability is used only on purpose
					put("rohanmerry", "103_85");  // Merry, Master Holbytla: the [rohan] Merry, a Hobbit companion; his texts are used only on purpose
					put("pippin", "1_306");       // Pippin, Friend to Frodo: a [shire] Hobbit companion who is no Merry; a bar on discarding tales only
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only

					put("slayer", "3_93");        // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; a regroup ability only
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerrysDaggerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Merry's Dagger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Merry.<br><b>Skirmish:</b> Exert Merry twice or discard this possession to wound a minion he is skirmishing. Any Shadow player may remove (1) to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dagger");

		assertEquals("Merry's Dagger", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnMerryAndNotOnAnotherHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.MoveCardsToHand(dagger);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(dagger));
		// Merry is the only character who may bear it, so it goes on him unasked; Pippin is no Merry.
		scn.FreepsPlayCard(dagger);

		assertAttachedTo(dagger, merry);
		assertTrue(scn.GetAttachedCards(pippin).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRohanMerryMayBearItToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var rohanmerry = scn.GetFreepsCard("rohanmerry");
		scn.MoveCompanionsToTable(rohanmerry);
		scn.MoveCardsToHand(dagger);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(dagger));
		scn.FreepsPlayCard(dagger);

		assertAttachedTo(dagger, rohanmerry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoMerryInPlayItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(dagger);

		scn.StartGame();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(dagger));
	}

	@Test
	public void WithBothCostsPayableHeIsAskedWhichAndExertingTwiceWoundsAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, dagger);

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);
		// Vitality 3 with 2 wounds: exhausted, so a wound kills it where an exertion could not be made.
		scn.AddWoundsToChar(savage, 2);

		assertAttachedTo(dagger, merry);
		assertEquals(2, scn.GetWoundsOn(savage));
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dagger));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(merry, slayer);
		// The Shadow player puts his other minion on Merry too.
		scn.ShadowAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.FreepsActionAvailable(dagger));
		scn.FreepsUseCardAction(dagger);

		assertTrue(scn.FreepsChoiceAvailable("Exert Merry twice"));
		assertTrue(scn.FreepsChoiceAvailable("Discard this possession"));
		scn.FreepsChoose("Exert Merry twice");
		assertEquals(2, scn.GetWoundsOn(merry));
		assertAttachedTo(dagger, merry);

		assertTrue(scn.ShadowDecisionAvailable("remove (1)"));
		scn.ShadowChooseNo();

		assertTrue(scn.FreepsHasCardChoiceAvailable(slayer, savage));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertInDiscard(savage);
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheRohanMerryExertsTwiceToWoundAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var rohanmerry = scn.GetFreepsCard("rohanmerry");
		scn.MoveCompanionsToTable(rohanmerry);
		scn.AttachCardsTo(rohanmerry, dagger);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);

		assertAttachedTo(dagger, rohanmerry);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rohanmerry, slayer);

		assertEquals(0, scn.GetWoundsOn(rohanmerry));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.FreepsActionAvailable(dagger));
		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Exert Merry twice");
		assertEquals(2, scn.GetWoundsOn(rohanmerry));
		scn.ShadowChooseNo();

		assertEquals(1, scn.GetWoundsOn(slayer));
		assertAttachedTo(dagger, rohanmerry);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardingTheDaggerPaysAndTheShadowPlayerMayRemove1ToPreventTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, dagger);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);

		assertAttachedTo(dagger, merry);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, slayer);

		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 1);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(slayer));
		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Discard this possession");

		assertInDiscard(dagger);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.ShadowDecisionAvailable("remove (1)"));
		scn.ShadowChooseYes();

		// The wound is prevented, and the dagger stays paid.
		assertEquals(twilight - 1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertInDiscard(dagger);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithMerryUnableToExertTwiceTheDaggerIsDiscardedAndWithAnEmptyPoolNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(merry, pippin);
		scn.AttachCardsTo(merry, dagger);
		// Vitality 4 with 2 wounds: he can exert once more, not twice.
		scn.AddWoundsToChar(merry, 2);

		var slayer = scn.GetShadowCard("slayer");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer);

		assertAttachedTo(dagger, merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(merry, slayer);

		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertTrue(scn.FreepsActionAvailable(dagger));
		scn.FreepsUseCardAction(dagger);

		// Only the discard can be paid, and it is paid unasked; Pippin, a Hobbit who could exert twice, is no Merry.
		assertInDiscard(dagger);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		// No (1) to remove: the Shadow player is not asked, and the Slayer takes the wound.
		assertFalse(scn.ShadowDecisionAvailable("remove (1)"));
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItWoundsNoMinionAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.AttachCardsTo(merry, dagger);

		var slayer = scn.GetShadowCard("slayer");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(slayer, savage);

		assertAttachedTo(dagger, merry);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, slayer}, new PhysicalCardImpl[]{merry, savage});
		// Aragorn's skirmish first; the Savage waits on Merry, whose own skirmish has not begun.
		scn.FreepsResolveSkirmish(aragorn);

		int twilight = scn.GetTwilight();
		// (1) is there to be removed, so the prevention is not asked for want of a pool, but for want of a wound.
		assertTrue(twilight >= 1);
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsActionAvailable(dagger));
		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Discard this possession");

		assertInDiscard(dagger);
		assertFalse(scn.ShadowDecisionAvailable("remove (1)"));
		assertEquals(twilight, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
