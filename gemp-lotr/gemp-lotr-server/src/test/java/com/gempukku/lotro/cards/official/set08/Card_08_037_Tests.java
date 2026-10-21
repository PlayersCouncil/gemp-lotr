package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_037_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("imrahil", "8_37");
					put("gknight", "5_35");     // Gondorian Knight: [gondor] Man companion, knight, strength 6; Knight is its whole text
					// Pippin, Trollslayer: a knight who is no Man (a [gondor] Hobbit); a skirmish ability used only on
					// purpose
					put("pippin", "103_31");
					// Ranger of Osgiliath: [gondor] Man companion who is no knight; a regroup ability used only on purpose
					put("ranger", "7_116");

					// Uruk Pursuer: [isengard] Uruk-hai, fierce, strength 5, vitality 3; his texts are about unbound
					// Hobbits, of whom none is killed here
					put("pursuer", "4_190");
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, vitality 3, no text
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
					// Goblin Runner: [moria] Orc, strength 5, vitality 1; its "when you play" is not met by a cheat
					put("runner", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ImrahilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Imrahil, Prince of Dol Amroth
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Knight</b>. To play, spot a knight.<br>Each time Imrahil wins a skirmish, you may exert 2 minions.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("imrahil");

		assertEquals("Imrahil", card.getBlueprint().getTitle());
		assertEquals("Prince of Dol Amroth", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.KNIGHT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAKnightIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imrahil = scn.GetFreepsCard("imrahil");
		var gknight = scn.GetFreepsCard("gknight");
		var ranger = scn.GetFreepsCard("ranger");
		// A [gondor] Man who is no knight (and Frodo).
		scn.MoveCompanionsToTable(ranger);
		scn.MoveCardsToHand(imrahil, gknight);

		scn.StartGame();
		assertInZone(Zone.HAND, imrahil, gknight);
		assertFalse(scn.FreepsPlayAvailable(imrahil));

		scn.FreepsPlayCard(gknight);
		assertInZone(Zone.FREE_CHARACTERS, gknight);
		assertTrue(scn.FreepsPlayAvailable(imrahil));
		scn.FreepsPlayCard(imrahil);
		assertInZone(Zone.FREE_CHARACTERS, imrahil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AKnightWhoIsNoManLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imrahil = scn.GetFreepsCard("imrahil");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToHand(imrahil);

		scn.StartGame();
		assertInZone(Zone.HAND, imrahil);
		assertTrue(scn.FreepsPlayAvailable(imrahil));
		scn.FreepsPlayCard(imrahil);
		assertInZone(Zone.FREE_CHARACTERS, imrahil);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachTimeImrahilWinsASkirmishHeMayExertTwoMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var imrahil = scn.GetFreepsCard("imrahil");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(imrahil, gknight);

		var pursuer = scn.GetShadowCard("pursuer");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, troop, savage, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{imrahil, pursuer}, new PhysicalCardImpl[]{gknight, troop});
		scn.ShadowAssignToMinions(gknight, savage, brigand);

		// Imrahil, 7, beats the Pursuer, 5, who takes the wound for losing.
		scn.FreepsResolveSkirmish(imrahil);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imrahil));
		scn.FreepsAcceptOptionalTrigger();

		// Minions of three cultures and races, in his skirmish and out of it; not the Brigand, who is exhausted
		// (vitality 1), and no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(pursuer, troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(brigand, imrahil, gknight, frodo));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(pursuer, troop);
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(imrahil));

		// The Gondorian Knight, 6 against 26, is killed.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, gknight);

		// The fierce round: Imrahil beats the Pursuer again, who dies of the wound, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(imrahil, pursuer);
		scn.FreepsResolveSkirmish(imrahil);
		scn.PassFierceSkirmishActions();
		assertInZone(Zone.DISCARD, pursuer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(imrahil));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenAnotherCompanionWinsOrImrahilLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imrahil = scn.GetFreepsCard("imrahil");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(imrahil, gknight);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gknight, runner}, new PhysicalCardImpl[]{imrahil, troop});

		// The Gondorian Knight, 6, beats the Runner, 5, with Imrahil in play and the Troop able to exert.
		scn.FreepsResolveSkirmish(gknight);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Imrahil, 7, loses to the Troop, 13 (not double his strength), and lives with one wound.
		scn.FreepsResolveSkirmish(imrahil);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(imrahil));
		assertInZone(Zone.FREE_CHARACTERS, imrahil);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void IsNotOfferedWhenEveryMinionLeftIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var imrahil = scn.GetFreepsCard("imrahil");
		var gknight = scn.GetFreepsCard("gknight");
		scn.MoveCompanionsToTable(imrahil, gknight);

		var pursuer = scn.GetShadowCard("pursuer");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, brigand);
		scn.AddWoundsToChar(pursuer, 1);
		assertEquals(1, scn.GetWoundsOn(pursuer));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{imrahil, pursuer}, new PhysicalCardImpl[]{gknight, brigand});

		// The Pursuer's wound for losing leaves him exhausted, and the Brigand (vitality 1) always is.
		scn.FreepsResolveSkirmish(imrahil);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
