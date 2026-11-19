package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hope", "7_22");
					put("lorien", "1_53");      // Lórien Elf: an Elf companion who is no archer, strength 4; "To play, spot an Elf" only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no archer, strength 6; stronger only against a Nazgul
					put("celeborn", "13_11");   // Celeborn, The Wise: a third Elf companion; his texts need a forest or a move in the regroup phase
					put("orophin", "1_56");     // Orophin, Lórien Bowman: an Elf ally; an archery ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no Elf, strength 7; a skirmish ability only
					put("valor", "4_87");       // Valor: [elven] Skirmish event: make an Elf strength +2
					// Sword-wall: [elven] "Skirmish or Regroup" event: spot 3 Elf companions to liberate a site or
					// exert a minion.
					put("swordwall", "4_84");
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] Skirmish event: make a [gondor] companion strength +2
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HopeComesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Hope Comes
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot 2 Elves.
		 * <b>Response:</b> If an [elven] skirmish event is played, discard this condition to draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hope");

		assertEquals("Hope Comes", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoElvesAreSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var lorien = scn.GetFreepsCard("lorien");
		var boromir = scn.GetFreepsCard("boromir");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(lorien, boromir);
		scn.MoveCardsToHand(hope, orophin);

		scn.StartGame();

		// One Elf, beside a companion who is no Elf.
		assertFalse(scn.FreepsPlayAvailable(hope));

		// The second Elf is an ally.
		scn.FreepsPlayCard(orophin);
		assertTrue(scn.FreepsPlayAvailable(hope));

		scn.FreepsPlayCard(hope);
		assertInZone(Zone.SUPPORT, hope);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElvenSkirmishEventPlayedDiscardsItToDrawThreeCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var arwen = scn.GetFreepsCard("arwen");
		var valor = scn.GetFreepsCard("valor");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(hope);
		scn.MoveCardsToHand(valor);
		assertInZone(Zone.SUPPORT, hope);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertInZone(Zone.SUPPORT, hope);
		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(6, scn.GetStrength(arwen));

		scn.FreepsPlayCard(valor);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hope));
		scn.FreepsAcceptOptionalTrigger();

		assertInDiscard(hope);
		// Valor has left the hand; the three cards drawn are all there is in it.
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 3, scn.GetFreepsDeckCount());
		// The event is still carried out: Arwen, the only Elf, is made strength +2.
		assertEquals(8, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SwordWallPlayedInASkirmishIsAnElvenSkirmishEventAndTheResponseMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var lorien = scn.GetFreepsCard("lorien");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		// Three Elf companions for Sword-wall.
		scn.MoveCompanionsToTable(lorien, arwen, celeborn);
		scn.MoveCardsToSupportArea(hope);
		scn.MoveCardsToHand(swordwall);
		assertInZone(Zone.SUPPORT, hope);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(1, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsPlayCard(swordwall);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(hope));
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, hope);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		// Sword-wall's own effect: no site to liberate, so the minion exerts.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SwordWallPlayedInTheRegroupPhaseIsNoSkirmishEventAndBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var lorien = scn.GetFreepsCard("lorien");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var swordwall = scn.GetFreepsCard("swordwall");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(lorien, arwen, celeborn);
		scn.MoveCardsToSupportArea(hope);
		scn.MoveCardsToHand(swordwall);
		assertInZone(Zone.SUPPORT, hope);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		// Arwen (6) beats the Uruk Savage (5), which takes a wound and is there in the regroup phase for
		// Sword-wall to exert.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(1, scn.GetFreepsHandCount());

		// "Skirmish or Regroup" played in the regroup phase is a regroup event (F-0292).
		scn.FreepsPlayCard(swordwall);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, hope);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ASkirmishEventOfAnotherCultureBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hope = scn.GetFreepsCard("hope");
		var boromir = scn.GetFreepsCard("boromir");
		var swordarm = scn.GetFreepsCard("swordarm");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToSupportArea(hope);
		scn.MoveCardsToHand(swordarm);
		assertInZone(Zone.SUPPORT, hope);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(1, scn.GetFreepsHandCount());

		scn.FreepsPlayCard(swordarm);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SUPPORT, hope);
		assertEquals(0, scn.GetFreepsHandCount());
		// The [gondor] event is carried out: Boromir, the only [gondor] companion, is strength +2.
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
