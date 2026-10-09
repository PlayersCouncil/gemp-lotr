package com.gempukku.lotro.cards.unofficial.rtmd.set_92_rotk;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_92_009_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: [rohan] Man companion, cost 3 (2 with a [rohan] Man to spot)
					put("theoden", "8_92");     // Theoden, Tall and Proud: when he is killed, you may play a [rohan] companion from your discard pile or draw deck
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a character who is no companion
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"92_9", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: [rohan] Man companion, cost 3 (2 with a [rohan] Man to spot)
					put("theoden", "8_92");     // Theoden, Tall and Proud: when he is killed, you may play a [rohan] companion from your discard pile or draw deck
					put("slayer", "3_93");      // Morgul Slayer: [sauron] Orc, strength 7, vitality 2; "Regroup: Exert this minion to wound a companion (except the Ring-bearer)."
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, cost 1; a character who is no companion
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "92_9"
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 92
		 * Name: Race Text 92_9
		 * Type: MetaSite
		 * Intensity: 4
		 * Game Text: While your fellowship is at site 6 or higher, companions cannot be played.
		 */

		var scn = GetFreepsScenario();

		var card = scn.GetFreepsCard("mod");

		assertEquals("Race Text 92_9", card.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, card.getBlueprint().getCardType());
		assertEquals(4, card.getBlueprint().getIntensity());
	}

	@Test
	public void CompanionCanBePlayed_BeforeSite6() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 5 or lower, companions should be playable normally.

		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(eomer));

		scn.SkipToSite(5);

		assertEquals(5, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionCannotBePlayed_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// At site 6+, companions cannot be played.

		var scn = GetFreepsScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		scn.SkipToSite(6);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsPlayAvailable(eomer));

		// "Or higher": nor at the sites beyond.
		scn.SkipToSite(7);
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, eomer);
		assertFalse(scn.FreepsPlayAvailable(eomer));

		scn.SkipToSite(8);
		assertEquals(8, scn.GetCurrentSiteNumber());
		assertInZone(Zone.HAND, eomer);
		assertFalse(scn.FreepsPlayAvailable(eomer));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyAndAPossessionCanStillBePlayed_AtSite6() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();

		var frodo = scn.GetRingBearer();
		var eomer = scn.GetFreepsCard("eomer");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCardsToHand(eomer, gimli, bounder, sword);

		scn.StartGame();
		scn.SkipToSite(6);

		assertEquals(6, scn.GetCurrentSiteNumber());
		// Companions of two cultures are refused; a character who is no companion and a possession are not.
		assertFalse(scn.FreepsPlayAvailable(eomer));
		assertFalse(scn.FreepsPlayAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(bounder));
		assertTrue(scn.FreepsPlayAvailable(sword));

		// Frodo is the only Hobbit in play to bear the sword.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);

		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);

		assertInZone(Zone.HAND, eomer);
		assertInZone(Zone.HAND, gimli);
		assertFalse(scn.FreepsPlayAvailable(eomer));
		assertFalse(scn.FreepsPlayAvailable(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CompanionCannotBePlayedFromDeck_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// Theoden plays a companion from the draw deck or discard pile.  This should also be blocked at site 6+.

		var scn = GetFreepsScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(6);

		scn.AddWoundsToChar(theoden, 2);
		scn.MoveCardsToBottomOfDeck(eomer);
		scn.MoveMinionsToTable(slayer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		//We now kill Theoden. His ability plays a companion from draw deck or discard pile; with neither branch
		// able to play anything, the optional trigger is not offered at all (same as any other play-from-zone
		// effect with no legal play) and we go straight back to the Regroup phase action procedure.
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, theoden);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertInZone(Zone.DECK, eomer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CompanionCannotBePlayedFromDiscard_AtSite6OrHigher() throws DecisionResultInvalidException, CardNotFoundException {
		// Theoden plays a companion from the draw deck or discard pile.  This should also be blocked at site 6+.

		var scn = GetFreepsScenario();

		var theoden = scn.GetFreepsCard("theoden");
		var eomer = scn.GetFreepsCard("eomer");
		var slayer = scn.GetShadowCard("slayer");

		scn.MoveCompanionsToTable(theoden);

		scn.StartGame();
		scn.SkipToSite(6);

		scn.AddWoundsToChar(theoden, 2);
		scn.MoveCardsToDiscard(eomer);
		scn.MoveMinionsToTable(slayer);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		//We now kill Theoden. His ability plays a companion from draw deck or discard pile; with neither branch
		// able to play anything, the optional trigger is not offered at all (same as any other play-from-zone
		// effect with no legal play) and we go straight back to the Regroup phase action procedure.
		scn.ShadowUseCardAction(slayer);
		assertInZone(Zone.DEAD, theoden);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		assertInZone(Zone.DISCARD, eomer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OpponentNotAffected() throws DecisionResultInvalidException, CardNotFoundException {
		// Shadow has the modifier.  Freeps playing a companion at site 6+ should still work.

		var scn = GetShadowScenario();

		var eomer = scn.GetFreepsCard("eomer");

		scn.MoveCardsToHand(eomer);

		scn.StartGame();
		scn.SkipToSite(6);

		assertEquals(6, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsPlayAvailable(eomer));

		scn.FreepsPlayCard(eomer);
		assertInZone(Zone.FREE_CHARACTERS, eomer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
