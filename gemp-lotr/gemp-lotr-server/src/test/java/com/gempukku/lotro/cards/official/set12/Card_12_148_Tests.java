package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_148_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("card", "12_148");
					put("uruk", "11_183");      // Feral Uruk: [uruk-hai] minion, strength 9; stronger at a battleground
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("pillage", "4_167");    // Pillage of Rohan: Shadow condition, plays on a site you control; idle with no Uruk-hai stacked there
					// Weary: Shadow condition, plays on a companion or ally; answers its bearer being assigned to an
					// [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8; a maneuver ability that is never used here
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; stronger against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally; his texts are for the start of a turn and the fellowship phase
					put("promise", "2_112");    // A Promise: Free Peoples condition, support area; needs Sam to do anything
					// The Tale of Gil-galad: Free Peoples condition, bearer must be an Elf; a skirmish ability
					put("tale1", "1_66");
					put("tale2", "1_66");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TempestofWarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Tempest of War
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot an [uruk-hai] minion, each unbound companion is resistance -1 for each condition he or she bears.<br><b>Skirmish:</b> Spot an [uruk-hai] minion and a companion who has resistance 2 or less to discard from play all conditions in all players' support areas.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");

		assertEquals("Tempest of War", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	// Uncomment any @Test markers below once this is ready to be used
	//@Test
	public void TempestofWarTest1() throws DecisionResultInvalidException, CardNotFoundException {
		//Pre-game setup
		var scn = GetScenario();

		var card = scn.GetFreepsCard("card");
		scn.MoveCardsToHand(card);

		scn.StartGame();
		scn.FreepsPlayCard(card);

		assertEquals(0, scn.GetTwilight());
	}

	@Test
	public void SkirmishAbilityDiscardsTheConditionsAnAllyAndAControlledSiteBearAndNotThoseACompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tempest = scn.GetShadowCard("card");
		var uruk = scn.GetShadowCard("uruk");
		var pinned = scn.GetShadowCard("pinned");
		var pillage = scn.GetShadowCard("pillage");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var promise = scn.GetFreepsCard("promise");
		var tale1 = scn.GetFreepsCard("tale1");
		var tale2 = scn.GetFreepsCard("tale2");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(promise, tempest, pinned);
		// In a support area: the conditions lying in either player's, those an ally bears (of either player's)
		// and the one on a site the Shadow player controls.
		scn.AttachCardsTo(elrond, tale1, weary1);
		scn.AttachCardsTo(site1, pillage);
		// In none: the conditions companions bear.
		scn.AttachCardsTo(arwen, tale2);
		scn.AttachCardsTo(aragorn, weary2);
		scn.MoveMinionsToTable(uruk);
		// The Ring-bearer's resistance is 10 less the burdens: 8 burdens leave 2
		scn.AddBurdens(8);

		scn.FreepsPass();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, uruk);
		scn.FreepsPass();

		assertEquals(2, scn.GetResistance(frodo));
		assertTrue(scn.ShadowActionAvailable(tempest));
		scn.ShadowUseCardAction(tempest);

		assertInDiscard(promise, tale1, weary1, pillage, pinned, tempest);
		assertAttachedTo(tale2, arwen);
		assertAttachedTo(weary2, aragorn);
		// The site is no condition and is still the Shadow player's
		assertTrue(scn.ShadowControls(site1));
		assertInZone(Zone.SUPPORT, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
