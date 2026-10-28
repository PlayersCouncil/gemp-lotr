package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "12_42");
					put("boromir", "1_96");     // Boromir, Lord of Gondor: [gondor] Man, strength 7; overwhelm text only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound [gondor] Man who is no Boromir, strength 8
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound [shire] Hobbit; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: an unbound [gondor] Hobbit; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer

					// Cards Boromir and Aragorn bear, one of each item class.
					put("shield", "1_107");     // Great Shield: a [gondor] shield; the minion archery total -1
					put("armor", "1_92");       // Armor: a [gondor] armor; no more than 1 wound each skirmish phase
					put("cloak", "1_42");       // Elven Cloak: an [elven] cloak; the minion archery total -1

					put("runner1", "1_178");    // Goblin Runner: strength 5, vitality 1; its text is about being played
					put("runner2", "1_178");
					put("runner3", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladeofGondorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Blade of Gondor, Sword of Boromir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Boromir.<br>He is <b>damage +1</b>.<br><b>Skirmish:</b> If Boromir is not assigned to a skirmish, discard a [gondor] card borne by him to have him replace an unbound Hobbit skirmishing a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("blade");

		assertEquals("Blade of Gondor", card.getBlueprint().getTitle());
		assertEquals("Sword of Boromir", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayableOnlyOnBoromirWhoIsThenDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(blade, boromir);

		scn.StartGame();

		// A [gondor] Man who is no Boromir cannot bear it.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(blade));

		scn.FreepsPlayCard(boromir);
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertTrue(scn.FreepsPlayAvailable(blade));
		scn.FreepsPlayCard(blade);

		// Boromir is its one bearer, so it goes on him unasked.
		assertEquals(boromir, blade.getAttachedTo());
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DiscardingAGondorCardHeBearsHasUnassignedBoromirReplaceTheUnboundHobbitSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var shield = scn.GetFreepsCard("shield");
		var armor = scn.GetFreepsCard("armor");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveCompanionsToTable(boromir, aragorn, merry, pippin);
		scn.AttachCardsTo(boromir, blade, shield, cloak);
		scn.AttachCardsTo(aragorn, armor);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertEquals(boromir, blade.getAttachedTo());
		assertEquals(boromir, shield.getAttachedTo());
		assertEquals(boromir, cloak.getAttachedTo());
		assertEquals(aragorn, armor.getAttachedTo());
		assertInZone(Zone.ATTACHED, blade, shield, cloak, armor);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// Merry fights first; Pippin is assigned to a skirmish still to come.  Boromir is assigned to none.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{pippin, runner2});
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertTrue(scn.IsCharAssignedAgainst(pippin, runner2));
		assertFalse(scn.IsCharAssigned(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);

		// Either [gondor] card he bears pays; his [elven] cloak does not, nor the [gondor] armor Aragorn bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(blade, shield));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(cloak, armor));
		scn.FreepsChooseCard(shield);

		assertInDiscard(shield);
		assertEquals(boromir, blade.getAttachedTo());
		assertEquals(boromir, cloak.getAttachedTo());
		assertEquals(aragorn, armor.getAttachedTo());
		// Boromir takes Merry's place; the Hobbit whose skirmish has not begun is left where he was.
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner1));
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharAssigned(merry));
		assertTrue(scn.IsCharAssignedAgainst(pippin, runner2));
		assertFalse(scn.IsCharSkirmishing(pippin));
		// CR 4.2 "replace": the skirmish goes on with Boromir; the Shadow player is next to act.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARingBoundHobbitOrAnUnboundManSkirmishingIsNotReplacedAndTheCardIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var shield = scn.GetFreepsCard("shield");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(boromir, aragorn, sam);
		scn.AttachCardsTo(boromir, blade, shield, armor);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		assertEquals(boromir, blade.getAttachedTo());
		assertEquals(boromir, shield.getAttachedTo());
		assertEquals(boromir, armor.getAttachedTo());
		assertInZone(Zone.ATTACHED, blade, shield, armor);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, runner1},
				new PhysicalCardImpl[]{aragorn, runner2});

		// Sam's skirmish: a Hobbit who is Ring-bound.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner1));
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);
		scn.FreepsChooseCard(shield);

		assertInDiscard(shield);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner1));
		assertFalse(scn.IsCharSkirmishing(boromir));
		assertFalse(scn.IsCharAssigned(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Aragorn's skirmish: an unbound companion who is no Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);
		scn.FreepsChooseCard(armor);

		assertInDiscard(armor);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, runner2));
		assertFalse(scn.IsCharSkirmishing(boromir));
		assertFalse(scn.IsCharAssigned(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileBoromirIsAssignedAndOfferedOnceHisSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade");
		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, merry, pippin);
		scn.AttachCardsTo(boromir, blade);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		var runner3 = scn.GetShadowCard("runner3");

		scn.StartGame();
		assertEquals(boromir, blade.getAttachedTo());
		assertInZone(Zone.ATTACHED, blade);
		scn.MoveMinionsToTable(runner1, runner2, runner3);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner1},
				new PhysicalCardImpl[]{boromir, runner2},
				new PhysicalCardImpl[]{pippin, runner3});

		// Merry's skirmish: an unbound Hobbit to replace and the Blade to pay with, but Boromir is assigned to a
		// skirmish still to come.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner1));
		assertTrue(scn.IsCharAssigned(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blade));
		scn.PassSkirmishActions();

		// Boromir's own skirmish: he is in it, and so not offered either.  Then his 7 + 2 beats the Runner's 5.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(blade));
		scn.PassSkirmishActions();

		// Pippin's skirmish: Boromir is assigned to nothing any more.  The Blade is the one [gondor] card he bears,
		// so it pays with no choice asked.
		scn.FreepsResolveSkirmish(pippin);
		assertFalse(scn.IsCharAssigned(boromir));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner3));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(blade));
		scn.FreepsUseCardAction(blade);

		assertInDiscard(blade);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, runner3));
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
