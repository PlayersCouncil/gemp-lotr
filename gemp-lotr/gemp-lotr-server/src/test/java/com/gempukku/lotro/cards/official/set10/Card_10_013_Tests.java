package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "10_13");
					// Sam, Son of Hamfast: a Hobbit, strength 3, vitality 4; a fellowship ability, and a response if
					// Frodo dies
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("party", "1_158");      // Uruk-hai Raiding Party: Uruk-hai, strength 9, vitality 3; damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Phial of Galadriel, Star-glass
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: 
		 * Game Text: Bearer must be a Hobbit.<br><b>Skirmish:</b> Until the regroup phase, remove the game text (except card type and race) from a minion skirmishing bearer and make it unable to gain game text. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertEquals("Star-glass", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesTheGameTextOfAMinionSkirmishingBearerAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("card");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, phial);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.SkipToAssignments();
		// The Savage fights the bearer; the Raiding Party waits for Aragorn, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{aragorn, party});
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertAttachedTo(phial, sam);
		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		// The minion skirmishing the bearer has lost its damage +1 and is an Uruk-hai minion still; the minion
		// assigned to Aragorn keeps its own.
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.IsType(savage, CardType.MINION));
		assertTrue(scn.IsRace(savage, Race.URUK_HAI));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertInDiscard(phial);

		// Sam's 3 loses to the Savage's 5 and is not overwhelmed: one wound, where damage +1 would have made two.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// Aragorn's 8 loses to the Raiding Party's 9, damage +1 and all: two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
