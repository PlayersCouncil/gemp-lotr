package com.gempukku.lotro.cards.official.set06;

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

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hobbitses", "6_42");
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability used only on purpose
					put("wraithgollum", "103_66"); // Gollum, Half a Wraith Himself: the [wraith] Gollum, strength 5; his abilities are used only on purpose
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: a [gollum] minion who is no Gollum, strength 8, fierce
					put("sam", "1_311");        // Sam, Son of Hamfast: strength 3, vitality 4; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NastyFoulHobbitsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Nasty, Foul Hobbitses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If Gollum wins a skirmish, add a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hobbitses");

		assertEquals("Nasty, Foul Hobbitses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenGollumWinsASkirmishTheShadowPlayerMayPlayItToAddABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("gollum");
		var sam = scn.GetFreepsCard("sam");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(hobbitses);

		scn.StartGame();
		assertInHand(hobbitses);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPass();

		// Nobody has won anything yet.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(hobbitses));

		// 5 against 3: Gollum wins.
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(hobbitses));
		scn.ShadowPlayCard(hobbitses);

		assertEquals(1, scn.FreepsGetBurdens());
		// The burden is the Ring-bearer's: no wound comes with it, on him or on the loser.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertInZone(Zone.DISCARD, hobbitses);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenTheWraithGollumWinsItIsPlayableToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("wraithgollum");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToHand(hobbitses);

		scn.StartGame();
		assertInHand(hobbitses);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();

		// 5 against 3: the [wraith] Gollum wins.
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.FreepsGetBurdens());
		assertTrue(scn.ShadowPlayAvailable(hobbitses));
		scn.ShadowPlayCard(hobbitses);

		assertEquals(1, scn.FreepsGetBurdens());
		assertInZone(Zone.DISCARD, hobbitses);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotPlayableWhenAGollumMinionWhoIsNoGollumWinsNorWhenGollumLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hobbitses = scn.GetShadowCard("hobbitses");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(hobbitses);

		scn.StartGame();
		assertInHand(hobbitses);
		scn.MoveMinionsToTable(gollum, shelob);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, shelob}, new PhysicalCardImpl[]{aragorn, gollum});

		// 8 against 6, not doubled: Shelob, a [gollum] minion who is no Gollum, wins.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		// The Shadow player is asked nothing: where the event can be played, he is asked here.
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// 5 against 8, not doubled: Gollum loses, and lives through his wound.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertInHand(hobbitses);
		assertEquals(0, scn.FreepsGetBurdens());
		// Shelob is fierce: the fierce assignments come next.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
