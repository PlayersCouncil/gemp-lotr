package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "2_69");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An Elf assigned to a [moria] Orc and a Dwarf assigned to an Uruk-hai, with two copies of the event in hand.
	protected VirtualTableScenario GetElfAndDwarfScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("differences1", "2_69");
					put("differences2", "2_69");
					put("arwen", "1_30");
					put("gimli", "1_13");
					put("runner", "1_178");
					put("savage", "1_151");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OldDifferencesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Old Differences
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an Elf or Dwarf skirmishing a [moria] Orc strength -1 (or -3 if you spot an Elf and a Dwarf).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Old Differences", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// "Make an Elf or Dwarf skirmishing a [moria] Orc strength -1": the definition took any Elf or Dwarf in play.
	@Test
	public void OnlyAnElfOrDwarfSkirmishingAMoriaOrcIsMadeWeaker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetElfAndDwarfScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);

		var differences1 = scn.GetShadowCard("differences1");
		var differences2 = scn.GetShadowCard("differences2");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToHand(differences1, differences2);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, runner}, new PhysicalCardImpl[]{gimli, savage});

		// Gimli is skirmishing an Uruk-hai, and Arwen's skirmish against the [moria] Orc has not begun: the event
		// finds nobody.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		scn.ShadowPlayCard(differences1);
		assertInZone(Zone.DISCARD, differences1);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(6, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 6 against 5: the Savage takes Gimli's two wounds and lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		// Arwen is skirmishing the [moria] Orc and is the only one who is: she is chosen with nothing asked, and
		// with an Elf and a Dwarf to spot she is strength -3.
		scn.FreepsResolveSkirmish(arwen);
		scn.FreepsPass();
		scn.ShadowPlayCard(differences2);
		assertEquals(3, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void OldDifferencesTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}
}
