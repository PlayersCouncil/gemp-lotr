package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "7_177");
					// put other cards in here as needed for the test case
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTransferScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade", "7_177");
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl, strength 9; a companion bearing a [wraith] condition is strength -2
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a Nazgûl, strength 10; her text needs 2 burdens or 2 wounds on the Ring-bearer
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1; no Nazgûl
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Glóin: strength 6, vitality 3
					put("farin", "1_11");       // Farin, Dwarven Emissary: strength 5, vitality 3; stronger only against an Orc
					// Four cards for the Free Peoples player's hand, which give him initiative
					put("guard1", "1_7");
					put("guard2", "1_7");
					put("guard3", "1_7");
					put("guard4", "1_7");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FeelHisBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Feel His Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>During the turn this card is transferred, the Shadow has initiative, regardless of the Free Peoples player's hand.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Feel His Blade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	// Ketura on Q-0034: an optional action with no cost that would do nothing is not offered.  The ability costs
	// nothing; with nobody skirmishing a Nazgûl, and once the condition has left the support area, there is
	// nothing for it to do.  It was offered all the same, and using it gave the Shadow initiative for the turn
	// though nothing was transferred.
	@Test
	public void TheTransferIsOfferedOnlyFromTheSupportAreaAndOnlyWhileSomebodyIsSkirmishingANazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTransferScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		scn.MoveCompanionsToTable(aragorn, gimli, farin);
		scn.MoveCardsToHand(scn.GetFreepsCard("guard1"), scn.GetFreepsCard("guard2"),
				scn.GetFreepsCard("guard3"), scn.GetFreepsCard("guard4"));

		var blade = scn.GetShadowCard("blade");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(blade);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, nelya, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{farin, savage}, new PhysicalCardImpl[]{aragorn, lemenya},
				new PhysicalCardImpl[]{gimli, nelya});

		// Farin's skirmish is against an Uruk-hai: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(farin);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsHasInitiative());
		scn.ShadowPass();

		// Aragorn's is against Úlairë Lemenya.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(blade));
		scn.ShadowUseCardAction(blade);
		assertAttachedTo(blade, aragorn);
		assertEquals(4, scn.GetFreepsHandCount());
		assertTrue(scn.ShadowHasInitiative());
		scn.PassSkirmishActions();

		// Gimli's is against Úlairë Nelya: somebody is skirmishing a Nazgûl, and the condition is no longer in the
		// support area it is to be transferred from.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, nelya));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(blade));
		assertAttachedTo(blade, aragorn);
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void FeelHisBladeTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(2, scn.GetTwilight());
	}
}
