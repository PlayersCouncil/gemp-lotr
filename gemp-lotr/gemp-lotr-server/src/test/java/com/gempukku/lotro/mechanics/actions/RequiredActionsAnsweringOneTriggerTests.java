package com.gempukku.lotro.mechanics.actions;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * Required actions that answer one trigger.
 *
 * Comprehensive Rules 4.2, "action": "If two or more required actions are occurring at the same time (for
 * example, more than one 'start of turn' action), the Free Peoples player decides in which order they occur."
 * Ketura on G-0005: "After all other procedures have been followed, the Free Peoples player is the 'tiebreaker'
 * on deciding timing of two simultaneous actions."
 *
 * The player is given the actions to order, each under its card's name, and they are carried out in the order
 * named.  The order can matter: where two required actions answer the same "is about to take a wound", the one
 * carried out first may leave the other nothing to do.  Comprehensive Rules 4.2, "instead": "the stated effect is
 * replaced with a different effect"; a wound that has already been prevented is no longer there to be replaced.
 */
public class RequiredActionsAnsweringOneTriggerTests
{
	protected VirtualTableScenario GetStartOfTurnScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Elrond, Lord of Rivendell, an ally whose home is site 3: "At the start of each of your
					// turns, heal every ally whose home is site 3."
					put("elrond", "1_40");
					// Grimir, Dwarven Elder: a Dwarf ally whose home is site 3, vitality 3; a fellowship ability
					// that is only ever used on purpose.  Elrond's text heals him and Dwarven Heart exerts him,
					// so which of the two comes first can be read on him.
					put("grimir", "1_17");
					// Dwarven Heart: "Bearer must be a Dwarf.  ...  At the start of each of your turns, exert
					// bearer."
					put("heart", "1_10");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli is the Ring-bearer because only a Gimli can bear Gimli's Helm, and he wears the Ring whose text is
	// required while it is worn.
	protected VirtualTableScenario GetWoundScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Gimli's Helm: "Bearer must be Gimli.  He takes no more than 1 wound during each skirmish
					// phase.  Skirmish: Discard Gimli's Helm to prevent all wounds to him."
					put("helm", "1_15");
					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				// Gimli, Bearer of Grudges: strength 6, vitality 3, resistance 4; damage +1.  "While Gimli is the
				// Ring-bearer, at the start of each skirmish involving him, add 2 burdens or 2 threats.  While
				// Gimli is damage +X, he is resistance +X."
				VirtualTableScenario.GimliRB,
				// The One Ring, Answer To All Riddles: vitality +2.  "While wearing The One Ring, the Ring-bearer
				// is strength +2, and each time he is about to take a wound in a skirmish, add a burden instead.
				// Skirmish: Add a burden to wear The One Ring until the regroup phase."
				VirtualTableScenario.ATARRing
		);
	}

	@Test
	public void TwoRequiredStartOfTurnActionsArePutToTheFreePeoplesPlayerToOrderAndBothAreCarriedOut() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartOfTurnScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var grimir = scn.GetFreepsCard("grimir");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveAlliesToTable(elrond, grimir);
		scn.AttachCardsTo(grimir, heart);
		scn.AddWoundsToChar(elrond, 1);

		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The turn begins, and the game stops to have the two ordered: neither has been carried out.
		scn.StartGame();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("Elrond"));
		assertTrue(scn.FreepsActionAvailable("Dwarven Heart"));
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// Elrond's heal first, which finds Grimir unwounded; then Dwarven Heart exerts him, with no second
		// question.  Both were carried out, in that order: the exertion is still on him.
		scn.FreepsResolveActionOrder("Elrond");
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetWoundsOn(grimir));
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheRequiredStartOfTurnActionNamedFirstIsCarriedOutFirst() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetStartOfTurnScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var grimir = scn.GetFreepsCard("grimir");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveAlliesToTable(elrond, grimir);
		scn.AttachCardsTo(grimir, heart);
		scn.AddWoundsToChar(elrond, 1);

		scn.StartGame();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// Dwarven Heart first this time: Grimir exerts, and Elrond's heal, coming after, takes the exertion
		// off again.  Named the other way round, he ends the same turn's start with a wound.
		scn.FreepsResolveActionOrder("Dwarven Heart");
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OfTwoRequiredAnswersToOneWoundTheOneCarriedOutFirstStopsItAndTheOtherThenDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWoundScenario();

		var gimli = scn.GetRingBearer();
		var ring = scn.GetRing();
		var helm = scn.GetFreepsCard("helm");
		scn.AttachCardsTo(gimli, helm);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		scn.SkipToAssignments();

		// Resistance 4, and 1 more for his damage +1: the four burdens this test adds leave him short of it.
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(0, scn.GetBurdens());

		// Gimli is the only companion, so 2 threats cannot be added: his own text adds 2 burdens.
		scn.FreepsAssignToMinions(gimli, party);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(2, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());

		// He puts on the Ring, for a burden.  Strength 6 + 2 is still under the Raiding Party's 9.
		scn.FreepsUseCardAction(ring);
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.RBWearingOneRing());
		assertEquals(8, scn.GetStrength(gimli));
		scn.ShadowPass();

		// And he spends the Helm: from here on all wounds to him are to be prevented.
		scn.FreepsUseCardAction(helm);
		assertInZone(Zone.DISCARD, helm);
		scn.ShadowPass();
		scn.FreepsPass();

		// He loses to a damage +1 minion: two wounds, one at a time.  The first is answered by two required
		// actions, the Ring's and the Helm's, and the Free Peoples player is asked which comes first.
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("The One Ring"));
		assertTrue(scn.FreepsActionAvailable("Gimli's Helm"));

		// The Helm first: the wound is prevented, and the Ring, coming second, has no wound to turn into a
		// burden.
		scn.FreepsResolveActionOrder("Gimli's Helm");
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetBurdens());

		// The second wound is put to him in the same way.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		assertEquals(2, scn.FreepsGetActionCount());
		assertTrue(scn.FreepsActionAvailable("The One Ring"));
		assertTrue(scn.FreepsActionAvailable("Gimli's Helm"));

		// The Ring first: a burden in place of the wound, and the Helm, coming second, has nothing to prevent.
		scn.FreepsResolveActionOrder("The One Ring");
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(4, scn.GetBurdens());

		// The skirmish is over and the Raiding Party is not fierce.
		assertInZone(Zone.SHADOW_CHARACTERS, party);
		assertFalse(scn.FreepsAwaitingRequiredResponseOrder());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
