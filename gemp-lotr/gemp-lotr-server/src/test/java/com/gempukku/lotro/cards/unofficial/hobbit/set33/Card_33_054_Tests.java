package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_054_Tests
{

	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer, the one Bilbo in play.
	// The skirmishes of turn 1 are at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("journey", "33_54");
					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] Dwarf companion, 7/4, damage +1; a fellowship ability
					put("gandalf", "30_26");    // Gandalf, Friend of Thorin: [gandalf] Wizard companion; a cost modifier and a fellowship ability
					put("pippin", "1_306");     // Pippin, Friend to Frodo: a [shire] Hobbit companion who is no Bilbo; a bar on discarding tales only

					// For the discard piles. Battle Fury: a [dwarven] skirmish event (a Dwarf character +3).
					// Noble intentions: a [dwarven] skirmish event (exert a companion who is no Bilbo: Bilbo +3).
					// Battle of Azanulbizar: a [dwarven] maneuver event. Take up arms. Fight!: a [gandalf] skirmish event
					// (spot Gandalf: a minion -3). Axe Strike: a [dwarven] skirmish event of set 1 (a Dwarf +2), a title
					// for the other player's discard pile alone.
					put("fury", "30_3");
					put("noble", "30_14");
					put("azanulbizar", "30_4");
					put("takeup", "30_31");
					put("axe", "1_3");

					put("spider", "31_55");     // Wicked Spider: [spider] Spider, 7/2, fierce; a skirmish ability that wants an Orc to discard
					put("bats", "32_30");       // Dark Bats: [moria] Creature, 4/2; a Shadow ability left unused
					put("footman", "30_42");    // Goblin Footman: [moria] Orc, 8/1; a "when you play" text, never reached
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void ThereandBackAgainStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: There and Back Again
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype:
		 * Resistance: +1
		 * Game Text: Bearer must be Bilbo.<br><b>Skirmish:</b> If Bilbo is skirmishing, add a doubt to play a [dwarven] skirmish event from your discard pile (limit once per phase).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("journey");

		assertEquals("There and Back Again", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void ThereAndBackAgainIsPlayedOnBilboAndOnNoOtherCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetFreepsCard("journey");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(thorin, pippin);
		scn.MoveCardsToHand(journey);

		scn.StartGame();
		assertInHand(journey);

		assertTrue(scn.FreepsPlayAvailable(journey));
		scn.FreepsPlayCard(journey);
		// Bilbo is its one bearer: no choice is put, though a Dwarf companion and a Hobbit companion who is no Bilbo
		// stand beside him.
		assertAttachedTo(journey, bilbo);
		assertTrue(scn.GetAttachedCards(thorin).isEmpty());
		assertTrue(scn.GetAttachedCards(pippin).isEmpty());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishActionAddsADoubtToPlayADwarvenSkirmishEventFromTheDiscardPileOnceInEachSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetFreepsCard("journey");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fury = scn.GetFreepsCard("fury");
		var noble = scn.GetFreepsCard("noble");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var takeup = scn.GetFreepsCard("takeup");
		var shadowAxe = scn.GetShadowCard("axe");
		var spider = scn.GetShadowCard("spider");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.AttachCardsTo(bilbo, journey);
		scn.MoveCardsToDiscard(fury, noble, azanulbizar, takeup, shadowAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(spider);
		// Bilbo's 3 raised to 8, to beat the fierce Spider's 7 twice.
		scn.ApplyAdHocModifier(new StrengthModifier(null, bilbo, null, 5));
		assertAttachedTo(journey, bilbo);
		assertInZone(Zone.DISCARD, fury, noble, azanulbizar, takeup, shadowAxe);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(7, scn.GetStrength(thorin));
		assertTrue(scn.FreepsActionAvailable(journey));
		scn.FreepsUseCardAction(journey);

		assertEquals(1, scn.GetBurdens());
		// [dwarven] skirmish events of the player's own discard pile; not a [dwarven] maneuver event, a [gandalf]
		// skirmish event, nor the [dwarven] skirmish event in the other player's discard pile.
		assertTrue(scn.FreepsHasCardChoiceAvailable(fury, noble));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(azanulbizar, takeup, shadowAxe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(fury);

		// Battle Fury is played on Thorin, its only Dwarf character.
		assertEquals(10, scn.GetStrength(thorin));
		assertInZone(Zone.DISCARD, fury, noble);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Once in this skirmish phase: Noble intentions is still there and a doubt could be added.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(journey));

		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(spider));
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, spider);
		scn.FreepsResolveSkirmish(bilbo);

		// The fierce skirmish is a phase of its own: offered again.
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(8, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(journey));
		scn.FreepsUseCardAction(journey);
		assertEquals(2, scn.GetBurdens());
		scn.FreepsChooseCardBPFromSelection(noble);
		// Noble intentions: a companion who is no Bilbo exerts, and Bilbo is strength +3.
		scn.FreepsChooseCard(thorin);
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(11, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileBilboIsAssignedButNotYetSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetFreepsCard("journey");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var fury = scn.GetFreepsCard("fury");
		var bats = scn.GetShadowCard("bats");
		var footman = scn.GetShadowCard("footman");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(bilbo, journey);
		scn.MoveCardsToDiscard(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(bats, footman);
		assertAttachedTo(journey, bilbo);
		assertInZone(Zone.DISCARD, fury);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, bats}, new PhysicalCardImpl[]{thorin, footman});
		// Thorin's skirmish first, while Bilbo waits for his.
		scn.FreepsResolveSkirmish(thorin);

		assertTrue(scn.IsCharAssigned(bilbo));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(journey));
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoDwarvenSkirmishEventInTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var journey = scn.GetFreepsCard("journey");
		var bilbo = scn.GetRingBearer();
		var thorin = scn.GetFreepsCard("thorin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var azanulbizar = scn.GetFreepsCard("azanulbizar");
		var takeup = scn.GetFreepsCard("takeup");
		var noble = scn.GetFreepsCard("noble");
		var shadowFury = scn.GetShadowCard("fury");
		var shadowAxe = scn.GetShadowCard("axe");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable(thorin, gandalf);
		scn.AttachCardsTo(bilbo, journey);
		scn.MoveCardsToDiscard(azanulbizar, takeup, shadowFury, shadowAxe);

		scn.StartGame();
		scn.MoveMinionsToTable(bats);
		assertAttachedTo(journey, bilbo);
		assertInZone(Zone.DISCARD, azanulbizar, takeup, shadowFury, shadowAxe);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, bats);
		scn.FreepsResolveSkirmish(bilbo);

		// Bilbo is skirmishing and a doubt could be added; the player's own pile holds a [dwarven] maneuver event
		// and a [gandalf] skirmish event, and the [dwarven] skirmish events are in the other player's pile.
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(journey));

		// With a [dwarven] skirmish event in his own pile, it is.
		scn.MoveCardsToDiscard(noble);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(journey));
	}
}
