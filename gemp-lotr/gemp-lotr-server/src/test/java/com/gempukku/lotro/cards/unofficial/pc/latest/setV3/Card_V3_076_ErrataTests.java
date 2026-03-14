package com.gempukku.lotro.cards.unofficial.pc.errata.setv03;

import com.gempukku.lotro.framework.*;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_V3_076_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nelya", "103_76");
					put("nazgul2", "1_234"); // Ulaire Nertea
					put("guard", "1_7");
					put("gimli", "1_12");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UlaireNelyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ulaire Nelya, Glorified to Conquer
		 * Unique: true
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgul
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 2
		 * Game Text: Fierce.
		* 	Skirmish: Exert this minion to make a Nazgul strength +1 for each of your sites on the adventure path
		*   (limit +2, or limit +5 if you can spot another Nazgul).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nelya");

		assertEquals("Ulaire Nelya", card.getBlueprint().getTitle());
		assertEquals("Glorified to Conquer", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(2, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void NelyaActivatedAbilityExertsAndBoostsNazgulInSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var nelya = scn.GetShadowCard("nelya");
		scn.MoveMinionsToTable(nelya);

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		scn.StartGame();

		// At site 1, shadow has 1 site on the adventure path.
		// With no other Nazgul, limit is +2. So 1 site = +1 (capped at +2).
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, nelya);
		scn.FreepsResolveSkirmish(gimli);

		// Nelya base STR is 11
		assertEquals(11, scn.GetStrength(nelya));
		assertEquals(0, scn.GetWoundsOn(nelya));

		// The errata changed this from a trigger to an activated ability.
		// It should be available as a "Use" action during skirmish.
		assertTrue(scn.ShadowActionAvailable(nelya));
		scn.ShadowUseCardAction(nelya);

		// Choose Nelya as the target Nazgul to boost
		scn.ShadowChooseCard(nelya);

		// Nelya should now be exerted (1 wound) and strength boosted
		assertEquals(1, scn.GetWoundsOn(nelya));
		// At site 1 with 1 shadow site on path, and no other Nazgul => limit +2, boost = +1
		assertEquals(12, scn.GetStrength(nelya));
	}
}
