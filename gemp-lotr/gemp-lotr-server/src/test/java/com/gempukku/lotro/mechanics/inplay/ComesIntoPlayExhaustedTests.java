package com.gempukku.lotro.mechanics.inplay;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * A character that "comes into play exhausted".
 *
 * Comprehensive Rules 4.2, "exhausted": "A character who has only 1 vitality remaining is exhausted."  Ketura on
 * Q-0302 (2026-10-14): "I'd say that "coming into play exhausted" pre-empts the actual play trigger, so it should
 * occur before any exertion responses or whatnot can occur."  The character is placed with the wounds that leave
 * it 1 vitality, before anything answers its play, and those wounds are no exertions.
 *
 * The game does it in PlayCardEffect, as the card is placed: for a play that says so itself (`exhausted: true` on
 * PlayCardFromHand, PlayCardFromDiscard and PlayCardFromDrawDeck: Peril 6_105, Gothmog 18_80; F-0500, engine
 * pass 37), and for a character another card says comes into play exhausted (the `ComesIntoPlayExhausted`
 * modifier: Goblin Domain 1_175, Blood of Númenor 2_31, Race Text 91_9, Not at Home 30_37; F-0501, engine pass 38).
 */
public class ComesIntoPlayExhaustedTests
{
	protected VirtualTableScenario GetGoblinDomainScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Goblin Domain: "Each time a companion is played to site 4 or higher, that companion comes
					// into play exhausted."
					put("domain", "1_175");
					// Out of Sight and Shot: "Each time a companion is played, the Free Peoples player must exert a
					// companion."
					put("outofsight", "7_204");
					// Determined Uruk: "Each time a companion exerts, you may draw a card."  Strength 7, vitality 2.
					put("uruk", "11_181");
					put("gimli", "1_13");       // Gimli, Son of Glóin: cost 2, vitality 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: cost 4, vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetPerilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Peril: "Regroup: Exert a [sauron] Orc to play a Nazgûl or [sauron] minion. Its twilight cost
					// is -4 and it comes into play exhausted."  A play from hand.
					put("peril", "6_105");
					put("chieftain", "1_266");      // Orc Chieftain: [sauron] Orc, vitality 2; answers a weapon played on him
					put("skirmisher", "1_257");     // Morgul Skirmisher: [sauron] Orc, cost 3, vitality 3; a maneuver ability
					put("scimitar", "1_269");       // Orc Scimitar: "Bearer must be a [sauron] Orc."
					// Úlairë Nertëa, Sanctified for Cruelty: "Each time you play a minion, you may exert it to play a
					// possession on it from your discard pile"
					put("nertea", "103_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Out of Sight and Shot answers a companion's play with an exertion the Free Peoples player chooses.  A companion
	// played fresh is among those he may choose; one that Goblin Domain has come into play exhausted is exhausted by
	// then, and is not.  Nobody is asked to order Goblin Domain against Out of Sight and Shot: Goblin Domain no
	// longer answers the play.
	@Test
	public void ACompanionThatComesIntoPlayExhaustedIsExhaustedBeforeItsPlayIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGoblinDomainScenario();

		var domain = scn.GetShadowCard("domain");
		var outofsight = scn.GetShadowCard("outofsight");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(domain, outofsight);
		scn.MoveCardsToHand(gimli, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);

		// The control: at site 3, one short of the site Goblin Domain names, Gimli comes into play fresh and is
		// one of the companions who may exert.
		scn.FreepsPlayCard(gimli);
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.SkipToSite(4);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(aragorn);

		// Aragorn is in play exhausted by the time his play is answered: Frodo and Gimli may exert, and he may not.
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// The wounds a character comes into play exhausted with are laid, not taken: he does not exert (Q-0302).
	// Determined Uruk answers each companion's exertion; it answers the one Out of Sight and Shot makes, and
	// nothing before it.
	@Test
	public void TheWoundsOfComingIntoPlayExhaustedAreNoExertions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGoblinDomainScenario();

		var domain = scn.GetShadowCard("domain");
		var outofsight = scn.GetShadowCard("outofsight");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(domain, outofsight);
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(aragorn);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveMinionsToTable(uruk);
		int shadowHand = scn.GetShadowHandCount();

		scn.FreepsPlayCard(aragorn);

		// Aragorn comes in with 3 wounds, and the first thing asked is Out of Sight and Shot's exertion.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, gimli));
		scn.FreepsChooseCard(gimli);

		// The control: Gimli's exertion is answered.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(uruk));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.ShadowHasOptionalTriggerAvailable(uruk));
		assertEquals(shadowHand, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	// A play that says itself that the card comes into play exhausted, from hand (`exhausted: true` on
	// PlayCardFromHand): the minion is exhausted when its play is answered, so Úlairë Nertëa cannot exert it to play
	// the Orc Scimitar on it.
	@Test
	public void AMinionPlayedFromHandExhaustedIsExhaustedBeforeItsPlayIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPerilScenario();

		var peril = scn.GetShadowCard("peril");
		var chieftain = scn.GetShadowCard("chieftain");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var scimitar = scn.GetShadowCard("scimitar");
		var nertea = scn.GetShadowCard("nertea");
		scn.MoveCardsToSupportArea(peril);
		scn.MoveCardsToHand(skirmisher);
		scn.MoveCardsToDiscard(scimitar);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.MoveMinionsToTable(chieftain, nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowUseCardAction(peril);

		// The Orc Chieftain, the one [sauron] Orc, exerts, and the Morgul Skirmisher, the one minion in hand, is
		// played with 2 wounds on him: Nertëa has nothing to offer.
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SHADOW_CHARACTERS, skirmisher);
		assertEquals(2, scn.GetWoundsOn(skirmisher));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertInDiscard(scimitar);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
