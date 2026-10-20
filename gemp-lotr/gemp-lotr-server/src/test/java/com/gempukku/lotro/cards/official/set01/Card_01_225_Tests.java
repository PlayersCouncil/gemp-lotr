package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_225_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "1_225");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: vitality 2; his text reads only companions bearing [wraith] conditions
					put("cantea", "1_230");     // Úlairë Cantëa, Lieutenant of Dol Guldur: another Nazgûl; fierce, a skirmish ability used only on purpose
					put("scout", "1_191");      // Moria Scout: [moria] Orc, strength 6, vitality 2; a play trigger only
					put("scimitar", "1_180");   // Goblin Scimitar: a possession (hand weapon) on a [moria] Orc; a play trigger only
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; a play trigger only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: an ARTIFACT hand weapon on a [gondor] Man; its response is used only on purpose
					put("bow", "1_90");         // Aragorn's Bow: a [gondor] possession, ranged weapon, on Aragorn; makes him an archer
					put("cloak", "1_42");       // Elven Cloak: an [elven] possession, cloak, on a companion; minion archery total -1
					put("boromir", "1_97");     // Boromir, Son of Denethor: a skirmish ability used only on purpose
					put("mail", "1_101");       // Coat of Mail: a [gondor] possession, armor, on a Man
					put("merry", "1_302");      // Merry, Friend to Sam: a skirmish ability used only on purpose
					put("hobbitsword", "1_299"); // Hobbit Sword: a possession (hand weapon) on a Hobbit, no other text
					put("maggot", "1_288");     // Farmer Maggot, Chaser of Rascals: a Hobbit ally, home 1F
					put("follow", "1_109");     // One Whom Men Would Follow: exert Aragorn to have an ally take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwordofMinasMorgulStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Sword of Minas Morgul
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Úlairë Lemenya.
		 * He is <b>damage +1</b>.
		 * <b>Skirmish:</b> Exert Úlairë Lemenya to discard a possession borne by a character he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Sword of Minas Morgul", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnUlaireLemenyaAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();

		scn.MoveMinionsToTable(lemenya, cantea);
		scn.MoveCardsToHand(sword);
		scn.SetTwilight(10);

		scn.FreepsPass();

		assertEquals(0, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(cantea, Keyword.DAMAGE));

		// Lemenya is the one bearer it may go on, and it goes on him with nothing asked; Cantëa, another Nazgûl,
		// is shown not to be a bearer by the board without Lemenya (CannotBePlayedWithoutUlaireLemenya).
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);

		assertAttachedTo(sword, lemenya);
		assertEquals(1, scn.GetKeywordCount(lemenya, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(cantea, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutUlaireLemenya() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();

		scn.MoveMinionsToTable(cantea);
		scn.MoveCardsToHand(sword);
		scn.SetTwilight(10);
		assertInHand(sword);

		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sword));
		assertInHand(sword);
	}

	@Test
	public void SkirmishActionExertsLemenyaToDiscardAPossessionBorneByTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var scout = scn.GetShadowCard("scout");
		var scimitar = scn.GetShadowCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var bow = scn.GetFreepsCard("bow");
		var cloak = scn.GetFreepsCard("cloak");
		var merry = scn.GetFreepsCard("merry");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.MoveCompanionsToTable(aragorn, merry);
		scn.AttachCardsTo(aragorn, narsil, cloak);
		scn.AttachCardsTo(merry, hobbitsword);

		scn.StartGame();

		scn.MoveMinionsToTable(lemenya, scout, runner);
		scn.AttachCardsTo(lemenya, sword);
		scn.AttachCardsTo(scout, scimitar);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(sword, lemenya);
		assertAttachedTo(scimitar, scout);

		scn.SkipToAssignments();
		// Merry's skirmish has not begun when Lemenya acts.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{merry, runner});
		// The Scout is a second minion in Aragorn's skirmish, bearing a possession of his own.
		scn.ShadowAssignToMinions(aragorn, scout);
		// The bow makes Aragorn an archer: laid after the archery phase.
		scn.AttachCardsTo(aragorn, bow);
		assertAttachedTo(bow, aragorn);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		// Two possessions of other cultures and classes on the companion he skirmishes; not the artifact beside
		// them, a possession on a companion whose skirmish is still to come, one on a minion in the same skirmish,
		// or this Sword itself.
		assertTrue(scn.ShadowHasCardChoiceAvailable(bow, cloak));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(narsil, hobbitsword, scimitar, sword));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(cloak);

		assertInDiscard(cloak);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(hobbitsword, merry);
		assertAttachedTo(scimitar, scout);
		assertAttachedTo(sword, lemenya);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APossessionBorneByAnAllyHeIsSkirmishingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var maggot = scn.GetFreepsCard("maggot");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var follow = scn.GetFreepsCard("follow");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(maggot);
		scn.AttachCardsTo(maggot, hobbitsword);
		scn.MoveCardsToHand(follow);

		scn.StartGame();

		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, sword);
		assertAttachedTo(hobbitsword, maggot);
		assertAttachedTo(sword, lemenya);

		// Set-up: Maggot takes part in skirmishes until the regroup phase.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPlayCard(follow);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(maggot, lemenya);
		scn.FreepsResolveSkirmish(maggot);
		assertTrue(scn.IsCharSkirmishingAgainst(maggot, lemenya));
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(hobbitsword);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InAnotherSkirmishLemenyaExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		var boromir = scn.GetFreepsCard("boromir");
		var mail = scn.GetFreepsCard("mail");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.AttachCardsTo(aragorn, cloak);
		scn.AttachCardsTo(boromir, mail);

		scn.StartGame();

		scn.MoveMinionsToTable(lemenya, runner);
		scn.AttachCardsTo(lemenya, sword);
		assertAttachedTo(cloak, aragorn);
		assertAttachedTo(mail, boromir);
		assertAttachedTo(sword, lemenya);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{boromir, runner});
		// Boromir's skirmish is fought first; Aragorn is assigned to Lemenya and waits for his.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner));
		scn.FreepsPass();

		// The cost can be paid, so the ability is offered; nobody is skirmishing Lemenya, so nothing is discarded:
		// not the possession of the companion fighting now, nor that of the companion assigned to him.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowActionAvailable(sword));
		scn.ShadowUseCardAction(sword);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertAttachedTo(mail, boromir);
		assertAttachedTo(cloak, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileLemenyaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lemenya = scn.GetShadowCard("lemenya");
		var cantea = scn.GetShadowCard("cantea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, cloak);

		scn.StartGame();

		// Vitality 3, so Cantëa, another Nazgûl, could pay an exertion if the cost were not Lemenya's own.
		scn.MoveMinionsToTable(lemenya, cantea);
		scn.AttachCardsTo(lemenya, sword);
		// Vitality 2: with 1 wound he cannot exert.
		scn.AddWoundsToChar(lemenya, 1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(cloak, aragorn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lemenya);
		// Cantëa is left unassigned.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, lemenya));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(sword));
		assertAttachedTo(cloak, aragorn);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(cantea));
	}
}
