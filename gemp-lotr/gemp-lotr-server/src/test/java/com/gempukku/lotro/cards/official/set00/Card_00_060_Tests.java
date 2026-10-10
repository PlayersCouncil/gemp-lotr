package com.gempukku.lotro.cards.official.set00;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_00_060_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "0_60");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTokenScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hat", "0_60");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a third companion, and the bearer of Desperate Defense
					put("smeagol", "7_71");     // Sméagol, Always Helps: a Free Peoples card that adds a burden (to be played) and 2 threats (his assignment ability)

					put("towers", "7_173");     // War Towers: [raider] Maneuver event, cost 1, "Spot a [raider] minion to add a threat" (one more for each companion over 4)
					put("nomad", "7_132");      // Desert Nomad: [raider] Man, vitality 2; damage +1 at 2 threats and nothing else
					put("defense", "1_244");    // Desperate Defense of the Ring: [sauron] condition on a companion; each time he is assigned, discard 3 cards from hand or add a burden
					put("runner", "1_178");     // Goblin Runner
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TomBombadilsHatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 0
		 * Name: Tom Bombadil's Hat
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support area
		 * Game Text: To play, spot 2 [shire] companions.<br>Each time a Shadow card adds a burden or a threat, add a [shire] token here.<br><b>Fellowship:</b> Remove 2 [shire] tokens from here to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Tom Bombadil's Hat", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AThreatAddedByAShadowCardAddsAShireTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var towers = scn.GetShadowCard("towers");
		var nomad = scn.GetShadowCard("nomad");
		scn.MoveCardsToSupportArea(hat);
		// Three companions with the Ring-bearer: room for 3 threats, and none over 4 for War Towers to count.
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveCardsToHand(towers);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetThreats());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(towers));
		scn.ShadowPlayCard(towers);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// Two threats added by a Free Peoples card, as the cost of Sméagol's ability, add no token.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.FreepsActionAvailable(smeagol));
		scn.FreepsUseCardAction(smeagol);
		assertEquals(3, scn.GetThreats());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
	}

	@Test
	public void ABurdenAddedByAShadowCardAddsAShireTokenHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTokenScenario();

		var hat = scn.GetFreepsCard("hat");
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var defense = scn.GetShadowCard("defense");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(hat);
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(smeagol);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, defense);
		scn.MoveMinionsToTable(runner);

		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetCultureTokensOn(hat));

		// The burden Sméagol adds to be played is added by a Free Peoples card: no token.
		scn.FreepsPlayCard(smeagol);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetCultureTokensOn(hat));
		assertEquals(0, scn.GetFreepsHandCount());

		// With no card in hand the Free Peoples player cannot discard 3, so Desperate Defense adds its burden
		// as Aragorn is assigned: a burden added by a Shadow card.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, runner);
		assertEquals(2, scn.GetBurdens());
		assertEquals(1, scn.GetCultureTokensOn(hat));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}
}
