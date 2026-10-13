package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_182_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_182");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("loathsome", "7_182");
					put("lemenya", "1_232");    // Úlairë Lemenya: a Nazgûl, strength 9, vitality 2; her text is about [wraith] conditions
					put("runner", "1_178");     // Goblin Runner: the minion of a skirmish that has not begun
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					put("spear", "7_108");      // Knight's Spear: a hand weapon, strength +2; a skirmish ability used only on purpose
					put("armor", "7_93");       // Footman's Armor: a response only when its bearer is about to be overwhelmed
					put("saga", "1_114");       // The Saga of Elendil: a condition he bears, and no possession
					put("gimli", "1_13");       // Gimli, Son of Glóin: the companion of the other skirmish
					put("dwarven", "1_8");      // Dwarven Armor: the possession he bears
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LoathsomeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Loathsome
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a skirmish that involved a Nazgûl is about to end, discard a possession borne by a companion in that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Loathsome", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAsASkirmishInvolvingANazgulIsAboutToEndDiscardsAPossessionBorneByACompanionInIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var loathsome = scn.GetShadowCard("loathsome");
		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		var armor = scn.GetFreepsCard("armor");
		var saga = scn.GetFreepsCard("saga");
		var gimli = scn.GetFreepsCard("gimli");
		var dwarven = scn.GetFreepsCard("dwarven");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, spear, armor, saga);
		scn.AttachCardsTo(gimli, dwarven);
		scn.MoveCardsToHand(loathsome);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lemenya}, new PhysicalCardImpl[]{gimli, runner});
		scn.SetTwilight(3);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 8 and the Spear's 2 against 9: it is the Spear that wins him the skirmish.
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(lemenya));
		// While the skirmish is being fought the event is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(loathsome));
		scn.ShadowPass();

		// The skirmish is resolved and Lemenya has taken the loser's wound: now it is about to end.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(loathsome));

		scn.ShadowPlayCard(loathsome);
		assertEquals(1, scn.GetTwilight());
		// The two possessions Aragorn bears; not the condition he bears, nor the armor of Gimli, who is assigned
		// to a skirmish that has not begun.
		assertTrue(scn.ShadowHasCardChoiceAvailable(spear, armor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(saga, dwarven));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(spear);

		assertInZone(Zone.DISCARD, spear, loathsome);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(dwarven, gimli);
		// The Spear that won the skirmish is gone, and the result stands (CR 4.2 on Loathsome: the discard does
		// not affect the resolution of that skirmish).
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
