package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_17_106_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_106");

					put("ship", "18_27");       // Ship of Smoke: unique [gandalf] pipeweed condition; its trigger needs Gandalf, and no Gandalf is here
					put("toby", "1_305");       // Old Toby: [shire] pipeweed possession, support area; an optional draw on its own play only
					put("toby2", "1_305");
					put("toby3", "1_305");
					put("pipe", "1_285");       // Bilbo's Pipe: a [shire] pipe on a Hobbit, no pipeweed; its fellowship ability is used only on purpose

					put("merry", "1_302");      // Merry, Friend to Sam: [shire] unbound Hobbit companion; a skirmish ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] unbound Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound Hobbit companion; abilities used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: an unbound companion who is no Hobbit, strength 8, vitality 4
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, vitality 3, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.GreatRing
		);
	}

	protected VirtualTableScenario GetGimliScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leaf", "17_106");

					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in)
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,   // Gimli, Bearer of Grudges: a Ring-bearer who is no Hobbit
				VirtualTableScenario.GreatRing
		);
	}

	@Test
	public void HalflingLeafStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Halfling Leaf
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: <b>Pipeweed</b>. To play, spot a Hobbit.<br><b>Skirmish:</b> Discard a pipeweed from play to cancel a skirmish involving an unbound Hobbit.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leaf");

		assertEquals("Halfling Leaf", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.PIPEWEED));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAHobbitIsSpottedAndAHobbitAllyIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var smeagol = scn.GetFreepsCard("smeagol");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(leaf);

		scn.StartGame();

		// Gimli is the Ring-bearer, and Smeagol is a [shire] companion who is no Hobbit.
		assertInHand(leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(leaf));

		scn.MoveAlliesToTable(bounder);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AHobbitCompanionWhoIsNotShireLetsItBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGimliScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(leaf);

		scn.StartGame();

		assertInHand(leaf);
		assertTrue(scn.FreepsPlayAvailable(leaf));

		scn.FreepsPlayCard(leaf);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAPipeweedFromPlayToCancelTheSkirmishOfAShireUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var ship = scn.GetFreepsCard("ship");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var pipe = scn.GetFreepsCard("pipe");
		var merry = scn.GetFreepsCard("merry");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, pipe);
		scn.MoveCardsToSupportArea(leaf, ship, toby);
		scn.MoveCardsToHand(toby2);
		assertInZone(Zone.SUPPORT, leaf, ship, toby);
		assertAttachedTo(pipe, merry);
		assertInHand(toby2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// Not in the phase before.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(leaf));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(merry, troop1);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.IsCharSkirmishingAgainst(merry, troop1));
		assertTrue(scn.FreepsActionAvailable(leaf));

		scn.FreepsUseCardAction(leaf);
		// Pipeweeds in play of two cultures and two types, this one among them; not the Old Toby in hand, and not
		// the pipe, which is no pipeweed.
		assertTrue(scn.FreepsHasCardChoiceAvailable(leaf, ship, toby));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(toby2, pipe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ship);

		assertInDiscard(ship);
		assertInZone(Zone.SUPPORT, leaf, toby);
		assertInHand(toby2);
		assertAttachedTo(pipe, merry);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharSkirmishing(troop1));
		// Nobody fought: 13 against 3 would have killed Merry.
		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheLeafMayDiscardItselfToCancelTheSkirmishOfAGondorUnboundHobbit() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var pippin = scn.GetFreepsCard("pippin");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(leaf);
		assertInZone(Zone.SUPPORT, leaf);

		scn.StartGame();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(pippin, troop1);
		scn.FreepsResolveSkirmish(pippin);

		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop1));
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.FreepsActionAvailable(leaf));

		// The only pipeweed in play is the Leaf itself, taken without a question.
		scn.FreepsUseCardAction(leaf);
		assertInDiscard(leaf);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharSkirmishing(troop1));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishOfARingBoundHobbitAnUnboundManOrAHobbitAllyIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leaf = scn.GetFreepsCard("leaf");
		var toby = scn.GetFreepsCard("toby");
		var toby2 = scn.GetFreepsCard("toby2");
		var toby3 = scn.GetFreepsCard("toby3");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var troop3 = scn.GetShadowCard("troop3");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(leaf, toby, toby2, toby3);
		assertInZone(Zone.SUPPORT, leaf, toby, toby2, toby3);

		scn.StartGame();
		scn.MoveCardsToHand(follow);
		scn.MoveMinionsToTable(troop1, troop2, troop3);
		scn.SkipToPhase(Phase.MANEUVER);
		// Bounder is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, troop1}, new PhysicalCardImpl[]{aragorn, troop2},
				new PhysicalCardImpl[]{bounder, troop3});

		// Sam is a Hobbit and Ring-bound.  The ability is used for nothing: its pipeweed is paid, and the
		// skirmish goes on.
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.FreepsActionAvailable(leaf));
		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(toby);
		assertInDiscard(toby);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Aragorn is unbound and no Hobbit.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertTrue(scn.FreepsActionAvailable(leaf));
		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(toby2);
		assertInDiscard(toby2);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();

		// Bounder is a Hobbit, and an ally: only companions are unbound.
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		assertTrue(scn.FreepsActionAvailable(leaf));
		scn.FreepsUseCardAction(leaf);
		scn.FreepsChooseCard(toby3);
		assertInDiscard(toby3);
		assertInZone(Zone.SUPPORT, leaf);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop3));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
