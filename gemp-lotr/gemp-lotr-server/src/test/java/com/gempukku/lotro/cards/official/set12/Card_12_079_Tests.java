package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_079_Tests
{

	// The Fellowship-block path: site 4, Balin's Tomb, is underground; sites 2 and 3, Ettenmoors and Rivendell
	// Valley, are not.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "12_79");
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Defend It and Hope: Maneuver event, exert a [gondor] Man to wound a minion once (twice an Uruk-hai)
					put("defend", "4_115");
					put("guard", "5_83");       // Household Guard: a [rohan] Man, valiant and nothing else
					// Rider's Mount: "At the start of each skirmish involving bearer, each minion skirmishing bearer must exert"
					put("mount", "4_287");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The King-block path: site 3, The Dimholt, is underground and a sanctuary; site 2, King's Tent, has no terrain.
	protected VirtualTableScenario GetDimholtScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("balrog", "12_79");
					put("troop", "1_177");
				}},
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheBalrogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: The Balrog, The Terror of Khazad-dûm
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 12
		 * Type: Minion
		 * Subtype: Balrog
		 * Strength: 17
		 * Vitality: 5
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>.<br>While The Balrog is at an underground site, it is <b>fierce</b> and cannot take wounds or be exerted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("balrog");

		assertEquals("The Balrog", card.getBlueprint().getTitle());
		assertEquals("The Terror of Khazad-dûm", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.BALROG, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(12, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(5, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtBalinsTombHeIsFierceAndAtRivendellValleyHeIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(balrog, troop);

		// Rivendell Valley, site 3: no underground.
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(balrog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));

		// The move to Balin's Tomb, site 4: underground.
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(balrog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
	}

	@Test
	public void AtTheDimholtHeIsFierceAndAtKingsTentHeIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetDimholtScenario();

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(balrog, troop);

		// King's Tent, site 2: no terrain.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(balrog, Keyword.FIERCE));

		// The move to The Dimholt, site 3: underground, and a sanctuary as well.
		scn.FreepsPass();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.HasKeyword(balrog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(troop, Keyword.FIERCE));
	}

	@Test
	public void AtBalinsTombHeCannotBeWoundedOrExertedWhileOtherMinionsAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(defend);

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(balrog, troop, troop2);

		// On to Balin's Tomb, site 4: underground.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(troop2));

		// Aragorn, the one [gondor] Man, exerts; the wound is offered on either Troop and not on The Balrog.
		assertTrue(scn.FreepsPlayAvailable(defend));
		scn.FreepsPlayCard(defend);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, troop2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(balrog));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(balrog));

		// The Balrog and the second Troop both on the Guard: at the start of that skirmish each minion
		// skirmishing him must exert, and only the Troop does.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, balrog}, new PhysicalCardImpl[]{aragorn, troop});
		scn.ShadowAssignToMinions(guard, troop2);
		scn.FreepsResolveSkirmish(guard);

		assertEquals(1, scn.GetWoundsOn(troop2));
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtEttenmoorsHeIsWoundedAndExertedAsAnyMinionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		var mount = scn.GetFreepsCard("mount");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, guard);
		scn.AttachCardsTo(guard, mount);
		scn.MoveCardsToHand(defend);

		var balrog = scn.GetShadowCard("balrog");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(mount, guard);
		scn.MoveMinionsToTable(balrog, troop);

		// The move to Ettenmoors, site 2: a plains, no underground.
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(balrog));

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(balrog, troop));
		scn.FreepsChooseCard(balrog);
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(troop));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, balrog}, new PhysicalCardImpl[]{aragorn, troop});
		scn.FreepsResolveSkirmish(guard);

		// The Mount's exertion: a second token on him.
		assertEquals(2, scn.GetWoundsOn(balrog));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
